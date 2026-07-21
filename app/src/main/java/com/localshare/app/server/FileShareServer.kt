package com.localshare.app.server

import android.content.Context
import android.util.Log
import com.localshare.app.data.AccessAction
import com.localshare.app.data.AccessLogBuffer
import com.localshare.app.data.AccessLogEntry
import com.localshare.app.data.FileRepository
import com.localshare.app.data.ShareConfig
import com.localshare.app.data.SharedFile
import com.localshare.app.data.FileCategory
import com.localshare.app.service.ServerForegroundService
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.localshare.app.R
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.LruCache
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Local HTTP server powered by NanoHTTPD.
 *
 * Routes:
 *   GET /                  → Web UI (HTML SPA)
 *   GET /api/files         → JSON listing of shared files
 *   GET /api/status        → Server status + connected device count
 *   POST /api/auth         → PIN authentication
 *   POST /api/upload       → Receive files from browser (laptop-to-phone)
 *   GET /download/{fileId} → Full file download
 *   GET /stream/{fileId}   → Stream with Range request support
 */
class FileShareServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    companion object {
        private const val TAG = "FileShareServer"
        /** Files larger than this are not fully buffered for AES-GCM (avoids OOM). */
        private const val MAX_ENCRYPT_IN_MEMORY_BYTES = 50L * 1024 * 1024
    }

    private val fileRepository = FileRepository(context)
    val accessLog = AccessLogBuffer()

    private val connectedIps = ConcurrentHashMap<String, Long>()
    private val _connectedDeviceCount = MutableStateFlow(0)
    val connectedDeviceCount: StateFlow<Int> = _connectedDeviceCount.asStateFlow()

    data class ConnectedClient(val ip: String, val lastSeenAt: Long)
    private val _connectedClients = MutableStateFlow<List<ConnectedClient>>(emptyList())
    val connectedClients: StateFlow<List<ConnectedClient>> = _connectedClients.asStateFlow()

    data class ActiveDownload(val ip: String, val filename: String, val progress: Float, val speedBytesPerSecond: Long, val fileId: Long = 0L)
    private val activeDownloadsMap = ConcurrentHashMap<String, ActiveDownload>()
    private val _activeDownloads = MutableStateFlow<List<ActiveDownload>>(emptyList())
    val activeDownloads: StateFlow<List<ActiveDownload>> = _activeDownloads.asStateFlow()

    private val _serverEvents = kotlinx.coroutines.flow.MutableSharedFlow<String>(extraBufferCapacity = 100)


    private val thumbnailCache = object : LruCache<Long, ByteArray>(20 * 1024 * 1024) { // 20MB cache
        override fun sizeOf(key: Long, value: ByteArray): Int = value.size
    }

    // Authenticated IPs (have entered the correct PIN)
    private val authenticatedIps = ConcurrentHashMap<String, Long>()

    // ─── Brute-force protection ─────────────────────────────────
    private data class AuthAttemptInfo(var attempts: Int = 0, var lockedUntil: Long = 0L)
    private val authAttempts = ConcurrentHashMap<String, AuthAttemptInfo>()
    private object BruteForceConfig {
        const val MAX_ATTEMPTS = 3
        const val LOCKOUT_MS = 30_000L
    }

    @Volatile
    var shareConfig: ShareConfig = ShareConfig()

    // Server settings — updated from app settings
    @Volatile
    var pin: String? = null

    @Volatile
    var deviceName: String = "LocalShare"

    @Volatile
    var maxConnections: Int = 3

    // ─── Encryption ──────────────────────────────────────────────
    @Volatile
    var encryptionEnabled: Boolean = false

    @Volatile
    private var encryptionKey: ByteArray? = null

    /**
     * Check if an encryption key has been generated.
     */
    fun hasEncryptionKey(): Boolean = encryptionKey != null

    /**
     * Generate a new random encryption key. Called when encryption is enabled.
     */
    fun generateEncryptionKey() {
        encryptionKey = FileEncryption.generateKey()
    }

    /**
     * Get the base64url-encoded encryption key for embedding in URLs.
     * Returns null if encryption is disabled.
     */
    fun getEncryptionKeyBase64(): String? {
        val key = encryptionKey ?: return null
        return FileEncryption.encodeKey(key)
    }

    /**
     * Set the encryption key from a base64url-encoded string (for client-side use).
     */
    fun setEncryptionKey(encodedKey: String) {
        encryptionKey = FileEncryption.decodeKey(encodedKey)
    }

    // ─── Clipboard Sync ────────────────────────────────────────
    @Volatile
    private var systemClipboard: String = ""
    private var sharedText: String = ""
    @Suppress("unused")
    private var phoneClipboardVersion: Long = 0

    // ─── Transfer Sessions (phone-to-phone push) ──────────────
    val activeSessions = ConcurrentHashMap<String, com.localshare.app.data.TransferSession>()

    /**
     * Callback for when a new transfer session is created.
     * Set by the ViewModel/Service to show accept/reject UI.
     */
    var onIncomingTransfer: ((com.localshare.app.data.TransferSession) -> Unit)? = null

    /**
     * Get the list of pending incoming sessions.
     */
    fun getPendingSessions(): List<com.localshare.app.data.TransferSession> {
        return activeSessions.values.filter { it.status == com.localshare.app.data.SessionStatus.PENDING }
    }

    /**
     * Accept a pending transfer session.
     */
    fun acceptSession(sessionId: String): Boolean {
        val session = activeSessions[sessionId] ?: return false
        activeSessions[sessionId] = session.copy(status = com.localshare.app.data.SessionStatus.ACTIVE)
        return true
    }

    /**
     * Reject a pending transfer session.
     */
    fun rejectSession(sessionId: String): Boolean {
        val session = activeSessions[sessionId] ?: return false
        activeSessions[sessionId] = session.copy(status = com.localshare.app.data.SessionStatus.REJECTED)
        return true
    }

    /**
     * Cancel an active transfer session.
     */
    fun cancelSession(sessionId: String): Boolean {
        val session = activeSessions[sessionId] ?: return false
        activeSessions[sessionId] = session.copy(status = com.localshare.app.data.SessionStatus.CANCELLED)
        return true
    }

    /**
     * Mark a session as completed.
     */
    fun completeSession(sessionId: String) {
        val session = activeSessions[sessionId] ?: return
        activeSessions[sessionId] = session.copy(status = com.localshare.app.data.SessionStatus.COMPLETED)
    }

    /**
     * Clean up old sessions (older than 5 minutes).
     */
    private fun cleanupSessions() {
        activeSessions.entries.removeIf { (_, session) ->
            session.status in listOf(
                com.localshare.app.data.SessionStatus.COMPLETED,
                com.localshare.app.data.SessionStatus.REJECTED,
                com.localshare.app.data.SessionStatus.CANCELLED,
                com.localshare.app.data.SessionStatus.FAILED
            )
        }
    }

    // ─── File list cache (avoids runBlocking deadlock under load) ────
    @Volatile
    private var cachedFiles: List<SharedFile> = emptyList()
    @Volatile
    private var cacheTime: Long = 0

    private fun getCachedFiles(): List<SharedFile> {
        return shareConfig.sharedFiles
    }

    private fun updateConnectedClients() {
        _connectedDeviceCount.value = connectedIps.size
        _connectedClients.value = connectedIps.map { ConnectedClient(it.key, it.value) }.sortedByDescending { it.lastSeenAt }
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        val ip = session.remoteIpAddress ?: "unknown"
        val method = session.method

        // Track connected device
        connectedIps[ip] = System.currentTimeMillis()
        cleanupStaleConnections()
        updateConnectedClients()

        Log.d(TAG, "$method $uri from $ip")

        return try {
            // ─── CORS preflight ────────────────────────────────
            if (method == Method.OPTIONS) {
                return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "").also {
                    it.addHeader("Access-Control-Allow-Origin", "*")
                    it.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    it.addHeader("Access-Control-Allow-Headers", "Content-Type, Range")
                    it.addHeader("Access-Control-Max-Age", "86400")
                }
            }

            // ─── Auth endpoint is always accessible ─────────────
            if (uri == "/api/auth" && method == Method.POST) {
                return handleAuth(session, ip)
            }

            // ─── Push transfer endpoints are always accessible ──
            if ((uri == "/api/prepare-upload" || uri == "/api/cancel" || uri == "/api/sessions") && method == Method.POST || uri == "/api/sessions" || uri == "/api/session/status") {
                return when {
                    uri == "/api/prepare-upload" && method == Method.POST -> handlePrepareUpload(session, ip)
                    uri == "/api/cancel" && method == Method.POST -> handleCancelTransfer(session, ip)
                    uri == "/api/session/status" && method == Method.GET -> handleSessionStatus(session)
                    uri == "/api/sessions" -> serveSessions()
                    else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
                }
            }

            // ─── PIN gate ───────────────────────────────────────
            val currentPin = pin
            if (currentPin != null && !isAuthenticated(ip)) {
                // Allow the main page (serves PIN entry UI) and icons
                val allowedPaths = setOf("/", "/index.html", "/favicon.ico", "/logo.png", "/logo-dark.png")
                if (uri !in allowedPaths && uri != "/api/auth") {
                    return newFixedLengthResponse(
                        Response.Status.UNAUTHORIZED,
                        "application/json",
                        """{"error":"PIN required","needsAuth":true}"""
                    )
                }
            }

            // ─── Max connections gate ───────────────────────────
            if (connectedIps.size > maxConnections && !connectedIps.containsKey(ip)) {
                return newFixedLengthResponse(
                    Response.Status.lookup(503),
                    MIME_PLAINTEXT,
                    "Server is at maximum capacity ($maxConnections devices). Please try again later."
                )
            }

            when {
                uri == "/" || uri == "/index.html" -> serveWebUI()
                uri == "/api/files" -> serveFileList(session)
                uri == "/api/files/clear" && method == Method.POST -> handleClearFiles()
                uri == "/api/status" -> serveStatus()
                // Encryption key only after PIN gate (never embed key in public share URLs)
                uri == "/api/encryption-key" && method == Method.GET -> serveEncryptionKey()
                uri == "/api/events" && method == Method.GET -> serveSSE()
                uri == "/api/clipboard" && method == Method.GET -> serveClipboard()
                uri == "/api/clipboard" && method == Method.POST -> handleSetClipboard(session)
                uri.startsWith("/download/") -> serveDownload(session, uri, ip)
                uri.startsWith("/stream/") -> serveStream(session, uri, ip)
                uri.startsWith("/api/thumbnail/") -> serveThumbnail(session, uri)
                uri.startsWith("/api/icon/") -> serveAppIcon(session, uri)
                uri == "/api/download-zip" -> serveZip(session, ip)
                uri == "/api/upload" && method == Method.POST -> handleUpload(session, ip)
                uri == "/api/prepare-upload" && method == Method.POST -> handlePrepareUpload(session, ip)
                uri == "/api/cancel" && method == Method.POST -> handleCancelTransfer(session, ip)
                uri == "/api/sessions" && method == Method.GET -> serveSessions()
                uri == "/favicon.ico" -> serveFavicon()
                uri == "/logo.png" -> serveLogo()
                uri == "/logo-dark.png" -> serveLogoDark()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving $uri", e)
            // Do not leak exception details to clients
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Internal Server Error"
            )
        }
    }

    // ─── Auth Handler ───────────────────────────────────────────

    private fun handleAuth(session: IHTTPSession, ip: String): Response {
        // Check brute-force lockout
        val attemptInfo = authAttempts[ip]
        if (attemptInfo != null) {
            val now = System.currentTimeMillis()
            if (attemptInfo.attempts >= BruteForceConfig.MAX_ATTEMPTS) {
                if (now < attemptInfo.lockedUntil) {
                    val retryAfter = ((attemptInfo.lockedUntil - now) / 1000).toInt() + 1
                    return newFixedLengthResponse(
                        Response.Status.TOO_MANY_REQUESTS,
                        "application/json",
                        """{"success":false,"error":"Too many attempts. Try again in $retryAfter seconds.","retryAfter":$retryAfter}"""
                    )
                } else {
                    // Lockout expired, reset
                    authAttempts.remove(ip)
                }
            }
        }

        // Parse POST body
        val bodyMap = HashMap<String, String>()
        try {
            session.parseBody(bodyMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing auth body", e)
        }

        val body = bodyMap["postData"] ?: ""
        val submittedPin = try {
            JSONObject(body).optString("pin", "")
        } catch (e: Exception) {
            ""
        }

        val currentPin = pin
        return if (currentPin != null && submittedPin == currentPin) {
            // Successful auth — clear attempts and authenticate IP
            authAttempts.remove(ip)
            authenticatedIps[ip] = System.currentTimeMillis()
            val json = JSONObject().apply {
                put("success", true)
                put("deviceName", deviceName)
            }
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                json.toString()
            )
        } else {
            // Failed auth — track attempt
            val info = authAttempts.getOrPut(ip) { AuthAttemptInfo() }
            info.attempts++
            if (info.attempts >= BruteForceConfig.MAX_ATTEMPTS) {
                info.lockedUntil = System.currentTimeMillis() + BruteForceConfig.LOCKOUT_MS
                Log.w(TAG, "IP $ip locked out after ${info.attempts} failed attempts")
            }
            val remaining = BruteForceConfig.MAX_ATTEMPTS - info.attempts
            val msg = if (remaining > 0) "Incorrect PIN ($remaining attempts left)" else "Too many attempts. Locked for 30 seconds."
            newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                "application/json",
                """{"success":false,"error":"$msg"}"""
            )
        }
    }

    private fun isAuthenticated(ip: String): Boolean {
        val authTime = authenticatedIps[ip] ?: return false
        // Auth expires after 24 hours
        return System.currentTimeMillis() - authTime < 24 * 60 * 60 * 1000
    }

    // ─── Route Handlers ─────────────────────────────────────────

    // ─── Clipboard Sync Handlers ────────────────────────────────

    /**
     * Update the system clipboard text from the Android side.
     * Called periodically by the ViewModel to keep the server aware of clipboard changes.
     * This will NOT overwrite explicitly shared text.
     */
    fun updatePhoneClipboard(text: String) {
        if (text != systemClipboard) {
            systemClipboard = text
            // Only bump version if there's no explicit shared text taking priority
            if (sharedText.isEmpty()) {
                phoneClipboardVersion++
            }
        }
    }

    /**
     * Set text explicitly shared by the user from the app's share dialog.
     * This takes priority over the system clipboard.
     */
    fun setSharedText(text: String) {
        sharedText = text
        phoneClipboardVersion++
    }

    /**
     * GET /api/clipboard — returns both system clipboard and shared text separately.
     */
    private fun serveClipboard(): Response {
        val json = JSONObject().apply {
            put("text", systemClipboard)
            put("sharedText", sharedText)
            put("version", phoneClipboardVersion)
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        ).also {
            it.addHeader("Access-Control-Allow-Origin", "*")
        }
    }

    /**
     * POST /api/clipboard — receives clipboard text from the laptop
     * and sets it on the phone's clipboard.
     */
    private fun handleSetClipboard(session: IHTTPSession): Response {
        val bodyMap = HashMap<String, String>()
        try {
            session.parseBody(bodyMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing clipboard body", e)
        }

        val body = bodyMap["postData"] ?: ""
        val text = try {
            JSONObject(body).optString("text", "")
        } catch (e: Exception) {
            ""
        }

        if (text.isNotEmpty()) {
            // Set clipboard on the main (UI) thread
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                        as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("LocalShare", text)
                    clipboard.setPrimaryClip(clip)
                    Log.d(TAG, "Clipboard set from laptop: ${text.take(50)}...")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to set clipboard", e)
                }
            }
        }

        val json = JSONObject().apply {
            put("success", true)
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        ).also {
            it.addHeader("Access-Control-Allow-Origin", "*")
        }
    }

    private fun serveWebUI(): Response {
        val needsAuth = pin != null
        val html = WebUI.getHtml(deviceName, needsAuth)
        return newFixedLengthResponse(Response.Status.OK, "text/html", html).also {
            it.addHeader("Cache-Control", "no-cache")
        }
    }

    // ─── Upload Handler (Laptop → Phone) ────────────────────────

    val newUploadedFiles = kotlinx.coroutines.flow.MutableSharedFlow<File>(extraBufferCapacity = 100)

    private fun handleUpload(session: IHTTPSession, ip: String): Response {
        try {
            val params = session.parameters
            val sessionId = params["sessionId"]?.firstOrNull()
            
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val localShareDir = File(downloadsDir, "LocalShare")
            if (!localShareDir.exists()) localShareDir.mkdirs()

            // ─── Phone-to-Phone Push Transfer ───
            if (sessionId != null) {
                val transferSession = activeSessions[sessionId]
                if (transferSession == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", """{"error":"Invalid sessionId"}""")
                }
                if (transferSession.status != com.localshare.app.data.SessionStatus.ACTIVE) {
                    return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json", """{"error":"Transfer not accepted yet or already finished"}""")
                }

                val filename = params["filename"]?.firstOrNull() ?: "upload_${System.currentTimeMillis()}"
                
                // Ensure unique filename
                val destFile = getUniqueFile(localShareDir, filename)
                
                val contentLength = session.headers["content-length"]?.toLongOrNull() ?: 0L
                var bytesCopied = 0L
                val startTime = System.currentTimeMillis()
                var lastUpdateTime = startTime
                var lastBytesCopied = 0L

                destFile.outputStream().use { output ->
                    val input = session.inputStream
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        
                        // Update progress in session
                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime > 500) { // Update every 500ms
                            val timeDiffSeconds = (now - lastUpdateTime) / 1000.0
                            val bytesDiff = bytesCopied - lastBytesCopied
                            val speed = if (timeDiffSeconds > 0) (bytesDiff / timeDiffSeconds).toLong() else 0L
                            
                            val remainingBytes = contentLength - bytesCopied
                            val eta = if (speed > 0) remainingBytes / speed else 0L
                            
                            activeSessions[sessionId] = transferSession.copy(
                                transferredBytes = transferSession.transferredBytes + bytesDiff,
                                speedBytesPerSecond = speed,
                                etaSeconds = eta
                            )
                            
                            lastUpdateTime = now
                            lastBytesCopied = bytesCopied
                        }
                    }
                }
                
                // Final update for this file
                activeSessions[sessionId] = transferSession.copy(
                    transferredBytes = transferSession.transferredBytes + (bytesCopied - lastBytesCopied),
                    speedBytesPerSecond = 0,
                    etaSeconds = 0
                )

                android.media.MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)
                newUploadedFiles.tryEmit(destFile)

                return newFixedLengthResponse(Response.Status.OK, "application/json", """{"success":true,"message":"File received"}""")
            }

            // ─── Browser Upload (Multipart) ───
            val bodyMap = HashMap<String, String>()
            session.parseBody(bodyMap)

            val uploadedFiles = mutableListOf<String>()
            val fileNames = params["filename"] ?: emptyList()
            
            for ((key, tempPath) in bodyMap) {
                if (!key.startsWith("file")) continue
                val tempFile = File(tempPath)
                if (!tempFile.exists()) continue

                val fileIndex = key.removePrefix("file").toIntOrNull()
                val rawName = if (fileIndex != null && fileIndex < fileNames.size) {
                    fileNames[fileIndex]
                } else {
                    "upload_${System.currentTimeMillis()}"
                }

                val destFile = getUniqueFile(localShareDir, rawName)

                val currentKey = encryptionKey
                val shouldDecrypt = encryptionEnabled && currentKey != null

                if (shouldDecrypt) {
                    try {
                        val encryptedBytes = tempFile.readBytes()
                        val decryptedBytes = FileEncryption.decrypt(encryptedBytes, currentKey!!)
                        destFile.writeBytes(decryptedBytes)
                    } catch (e: Exception) {
                        Log.e(TAG, "Decryption failed for upload: ${tempFile.name}", e)
                        // If decryption fails, we'll save the raw file as a fallback, or we can skip it.
                        // Let's just write the raw bytes so data is not completely lost
                        tempFile.copyTo(destFile, overwrite = true)
                    }
                } else {
                    tempFile.copyTo(destFile, overwrite = true)
                }

                uploadedFiles.add(destFile.name)
                newUploadedFiles.tryEmit(destFile)

                android.media.MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, null)

                Log.d(TAG, "Received file: ${destFile.name} from $ip")
                accessLog.add(AccessLogEntry(ip = ip, filename = destFile.name, action = AccessAction.UPLOAD))
            }

            val json = JSONObject().apply {
                put("success", true)
                put("files", JSONArray(uploadedFiles))
                put("message", "${uploadedFiles.size} file(s) received")
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            val json = JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "Upload failed")
            }
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", json.toString())
        }
    }

    private fun getUniqueFile(dir: File, filename: String): File {
        val safeName = filename
            .replace(Regex("[/\\\\]"), "_")
            .replace(Regex("^\\.+"), "")
            .ifBlank { "upload_${System.currentTimeMillis()}" }
        
        val destFile = File(dir, safeName)
        if (!destFile.exists()) return destFile

        val base = destFile.nameWithoutExtension
        val ext = destFile.extension
        var counter = 1
        var candidate: File
        do {
            candidate = File(dir, "${base}_${counter}.${ext}")
            counter++
        } while (candidate.exists())
        return candidate
    }

    // ─── Phone-to-Phone Push Transfer Handlers ─────────────────

    /**
     * POST /api/prepare-upload
     * Sender sends file metadata. Receiver shows accept/reject dialog.
     * Body: { "senderName": "...", "files": [ { "id", "fileName", "size", "fileType" } ] }
     * Response: { "sessionId": "...", "status": "pending" }
     */
    private fun handlePrepareUpload(session: IHTTPSession, ip: String): Response {
        try {
            val bodyMap = HashMap<String, String>()
            session.parseBody(bodyMap)
            val body = bodyMap["postData"] ?: ""
            val json = JSONObject(body)

            val senderName = json.optString("senderName", "Unknown Device")
            val filesJson = json.getJSONArray("files")

            val files = mutableListOf<com.localshare.app.data.FileInfo>()
            var totalSize = 0L
            for (i in 0 until filesJson.length()) {
                val fileJson = filesJson.getJSONObject(i)
                val fileInfo = com.localshare.app.data.FileInfo(
                    id = fileJson.optString("id", "file_$i"),
                    fileName = fileJson.getString("fileName"),
                    size = fileJson.optLong("size", 0),
                    fileType = fileJson.optString("fileType", "application/octet-stream"),
                    sha256 = fileJson.optString("sha256", null)
                )
                files.add(fileInfo)
                totalSize += fileInfo.size
            }

            val sessionId = "session_${System.currentTimeMillis()}_${ip.replace(".", "")}"
            val transferSession = com.localshare.app.data.TransferSession(
                sessionId = sessionId,
                senderName = senderName,
                senderIp = ip,
                files = files,
                totalSize = totalSize,
                status = com.localshare.app.data.SessionStatus.PENDING
            )

            activeSessions[sessionId] = transferSession
            cleanupSessions()

            // Notify the app layer (ViewModel/Service) about incoming transfer
            onIncomingTransfer?.invoke(transferSession)

            Log.d(TAG, "Incoming transfer from $senderName ($ip): ${files.size} files, $totalSize bytes")

            val response = JSONObject().apply {
                put("sessionId", sessionId)
                put("status", "pending")
                put("message", "Waiting for receiver to accept")
            }

            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                response.toString()
            ).also {
                it.addHeader("Access-Control-Allow-Origin", "*")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Prepare upload error", e)
            val json = JSONObject().apply {
                put("success", false)
                put("error", e.message ?: "Invalid request")
            }
            return newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json",
                json.toString()
            )
        }
    }

    private fun handleClearFiles(): Response {
        ServerForegroundService.triggerClearFiles()
        return newFixedLengthResponse(Response.Status.OK, "application/json", """{"status":"cleared"}""")
    }

    /**
     * POST /api/cancel
     * Cancel an active transfer session.
     * Body: { "sessionId": "..." }
     */
    private fun handleCancelTransfer(session: IHTTPSession, ip: String): Response {
        try {
            val bodyMap = HashMap<String, String>()
            session.parseBody(bodyMap)
            val body = bodyMap["postData"] ?: ""
            val json = JSONObject(body)
            val sessionId = json.optString("sessionId", "")

            if (sessionId.isEmpty()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    """{"error":"Missing sessionId"}"""
                )
            }

            val cancelled = cancelSession(sessionId)
            val response = JSONObject().apply {
                put("success", cancelled)
                put("message", if (cancelled) "Session cancelled" else "Session not found")
            }

            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                response.toString()
            ).also {
                it.addHeader("Access-Control-Allow-Origin", "*")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cancel transfer error", e)
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                """{"error":"Cancel failed"}"""
            )
        }
    }

    /**
     * GET /api/session/status?sessionId=...
     * Get the status of a specific transfer session.
     */
    private fun handleSessionStatus(session: IHTTPSession): Response {
        val params = session.parameters
        val sessionId = params["sessionId"]?.firstOrNull() ?: ""
        if (sessionId.isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", """{"error":"Missing sessionId"}""")
        }

        val transferSession = activeSessions[sessionId]
        if (transferSession == null) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", """{"error":"Session not found"}""")
        }

        val json = JSONObject().apply {
            put("sessionId", transferSession.sessionId)
            put("status", transferSession.status.name)
            put("transferredBytes", transferSession.transferredBytes)
            put("speedBytesPerSecond", transferSession.speedBytesPerSecond)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString()).also {
            it.addHeader("Access-Control-Allow-Origin", "*")
        }
    }

    /**
     * GET /api/sessions
     * Get list of active sessions (for the receiver to check status).
     */
    private fun serveSessions(): Response {
        val sessions = activeSessions.values.map { session ->
            JSONObject().apply {
                put("sessionId", session.sessionId)
                put("senderName", session.senderName)
                put("senderIp", session.senderIp)
                put("totalSize", session.totalSize)
                put("fileCount", session.files.size)
                put("status", session.status.name.lowercase())
            }
        }

        val response = JSONObject().apply {
            put("sessions", JSONArray(sessions))
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            response.toString()
        ).also {
            it.addHeader("Access-Control-Allow-Origin", "*")
        }
    }

    private fun serveFileList(session: IHTTPSession): Response {
        @Suppress("DEPRECATION")
        val params = session.parms ?: emptyMap()
        val categoryFilter = params["category"]

        val files = getCachedFiles()

        val filtered = if (categoryFilter != null && categoryFilter != "all") {
            files.filter { it.category.name.equals(categoryFilter, ignoreCase = true) }
        } else {
            files
        }

        val jsonArray = JSONArray()
        for (file in filtered) {
            val resolvedMime = resolveStreamMimeType(file.name, file.mimeType)
            val isStreamable = resolvedMime.startsWith("video/") || resolvedMime.startsWith("audio/") || resolvedMime.startsWith("image/")
            val typeIcon = when {
                resolvedMime.startsWith("video/") -> "video"
                resolvedMime.startsWith("image/") -> "image"
                resolvedMime.startsWith("audio/") -> "audio"
                resolvedMime.startsWith("text/") -> "document"
                resolvedMime.contains("pdf") -> "pdf"
                resolvedMime.contains("zip") || resolvedMime.contains("rar") || resolvedMime.contains("tar") -> "archive"
                resolvedMime.contains("apk") -> "android"
                resolvedMime == "application/x-directory" -> "folder"
                else -> "file"
            }
            
            val obj = JSONObject().apply {
                put("id", file.id)
                put("name", file.name)
                put("size", file.size)
                put("formattedSize", file.formattedSize)
                put("mimeType", resolvedMime)
                put("category", file.category.name.lowercase())
                put("typeIcon", typeIcon)
                put("isStreamable", isStreamable)
                put("lastModified", file.lastModified)
            }
            jsonArray.put(obj)
        }

        val responseJson = JSONObject().apply {
            put("files", jsonArray)
            put("count", filtered.size)
            put("connectedDevices", connectedIps.size)
            put("pinProtected", pin != null)
            put("encrypted", encryptionEnabled && encryptionKey != null)
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            responseJson.toString()
        ).also {
            it.addHeader("Access-Control-Allow-Origin", "*")
        }
    }

    private fun serveStatus(): Response {
        val json = JSONObject().apply {
            put("running", true)
            put("connectedDevices", connectedIps.size)
            put("maxConnections", maxConnections)
            put("deviceName", deviceName)
            put("pinProtected", pin != null)
            put("encrypted", encryptionEnabled && encryptionKey != null)
            put("sharedFileCount", getCachedFiles().size)
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        )
    }

    private fun serveDownload(session: IHTTPSession, uri: String, ip: String): Response {
        val fileId = extractFileId(uri)
        Log.d(TAG, "serveDownload: uri=$uri fileId=$fileId")
        if (fileId == null) return notFound()
        val file = findFile(fileId)
        Log.d(TAG, "serveDownload: file=${file?.name} mime=${file?.mimeType} path=${file?.path}")
        if (file == null) return notFound()

        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.DOWNLOAD))

        val currentKey = encryptionKey
        val shouldEncrypt = encryptionEnabled && currentKey != null

        // Virtual Text Files (pasted text) — not resumable
        if (file.mimeType == "text/plain" && file.path.startsWith("virtual://")) {
            val textContent = file.path.removePrefix("virtual://")
            val bytes = textContent.toByteArray()
            val responseBytes = if (shouldEncrypt) FileEncryption.encrypt(bytes, currentKey!!) else bytes
            val response = newFixedLengthResponse(Response.Status.OK, "application/octet-stream", ByteArrayInputStream(responseBytes), responseBytes.size.toLong())
            response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
            if (shouldEncrypt) response.addHeader("X-Encrypted", "aes-256-gcm")
            addDownloadHeaders(response)
            return response
        }

        // Folder ZIP — write to disk temp file, then stream
        if (file.mimeType == "application/x-directory") {
            val tempFile = File.createTempFile("folder_", ".zip", context.cacheDir)
            tempFile.deleteOnExit()
            try {
                ZipOutputStream(tempFile.outputStream()).use { zos ->
                    zipFolder(context, file.uri, zos, "${file.name}/")
                }

                if (tempFile.length() > 0) {
                    if (shouldEncrypt) {
                        // Read zip, encrypt, serve as fixed response
                        val zipBytes = tempFile.readBytes()
                        tempFile.delete()
                        val encryptedBytes = FileEncryption.encrypt(zipBytes, currentKey!!)
                        val res = newFixedLengthResponse(
                            Response.Status.OK, "application/octet-stream",
                            ByteArrayInputStream(encryptedBytes), encryptedBytes.size.toLong()
                        )
                        res.addHeader("Content-Disposition", "attachment; filename=\"${file.name}.zip\"")
                        res.addHeader("X-Encrypted", "aes-256-gcm")
                        addDownloadHeaders(res)
                        return res
                    } else {
                        val res = newChunkedResponse(
                            Response.Status.OK,
                            "application/zip",
                            FileInputStream(tempFile)
                        )
                        res.addHeader("Content-Disposition", "attachment; filename=\"${file.name}.zip\"")
                        addDownloadHeaders(res)
                        return res
                    }
                }
                tempFile.delete()
                return notFound()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating zip for folder: ${file.name}", e)
                tempFile.delete()
                return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    MIME_PLAINTEXT,
                    "Error creating zip"
                )
            }
        }

        // ─── Encrypted download (full file, no Range) ────────────
        // Skip in-memory encrypt for very large files to avoid OOM; fall back to plain stream.
        if (shouldEncrypt) {
            val physicalFile = File(file.path)
            val sizeHint = when {
                physicalFile.exists() -> physicalFile.length()
                file.size > 0 -> file.size
                else -> -1L
            }
            if (sizeHint > MAX_ENCRYPT_IN_MEMORY_BYTES) {
                Log.w(TAG, "File too large for in-memory encrypt (${sizeHint}B): ${file.name}; serving plain")
            } else {
                try {
                    val fileBytes = if (physicalFile.exists() && physicalFile.canRead()) {
                        if (physicalFile.length() > MAX_ENCRYPT_IN_MEMORY_BYTES) {
                            Log.w(TAG, "Abort encrypt for large file: ${file.name}")
                            null
                        } else {
                            physicalFile.readBytes()
                        }
                    } else {
                        context.contentResolver.openInputStream(file.uri)?.use { stream ->
                            val limited = ByteArrayOutputStream()
                            val buf = ByteArray(8192)
                            var total = 0L
                            var n: Int
                            var tooLarge = false
                            while (stream.read(buf).also { n = it } != -1) {
                                total += n
                                if (total > MAX_ENCRYPT_IN_MEMORY_BYTES) {
                                    tooLarge = true
                                    break
                                }
                                limited.write(buf, 0, n)
                            }
                            if (tooLarge) null else limited.toByteArray()
                        }
                    }
                    if (fileBytes != null) {
                        val encryptedBytes = FileEncryption.encrypt(fileBytes, currentKey!!)
                        val response = newFixedLengthResponse(
                            Response.Status.OK, "application/octet-stream",
                            ByteArrayInputStream(encryptedBytes), encryptedBytes.size.toLong()
                        )
                        response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
                        response.addHeader("X-Encrypted", "aes-256-gcm")
                        addDownloadHeaders(response)
                        return response
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error encrypting file for download: ${file.name}", e)
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Encryption error")
                }
            }
        }

        // ─── Plain Resumable Downloads via Range Requests ────────
        val rangeHeader = session.headers?.get("range")
        val resolvedMimeType = resolveStreamMimeType(file.name, file.mimeType)

        // Try file path first (supports efficient seeking)
        val physicalFile = File(file.path)
        return if (physicalFile.exists() && physicalFile.canRead()) {
            val response = RangeRequestHandler.createResponse(physicalFile, resolvedMimeType, rangeHeader)
            response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
            addDownloadHeaders(response)
            response
        } else {
            // Fallback to content resolver
            try {
                val inputStream = context.contentResolver.openInputStream(file.uri)
                    ?: return notFound()
                val response = RangeRequestHandler.createResponseFromStream(
                    inputStream, file.size, resolvedMimeType, rangeHeader
                )
                response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
                addDownloadHeaders(response)
                response
            } catch (e: Exception) {
                Log.e(TAG, "Error opening file for download: ${file.name}", e)
                notFound()
            }
        }
    }

    private fun addDownloadHeaders(response: Response) {
        response.addHeader("X-Content-Type-Options", "nosniff")
        response.addHeader("X-Frame-Options", "DENY")
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate")
        response.addHeader("Pragma", "no-cache")
    }

    private fun zipFolder(context: Context, treeUri: Uri, zos: ZipOutputStream, basePath: String) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
        if (rootDoc == null) {
            Log.e(TAG, "zipFolder: DocumentFile.fromTreeUri returned null for $treeUri")
            return
        }
        Log.d(TAG, "zipFolder: root=$basePath children=${rootDoc.listFiles().size}")
        zipDocumentFile(rootDoc, zos, basePath)
    }

    private fun zipDocumentFile(doc: DocumentFile, zos: ZipOutputStream, basePath: String) {
        doc.listFiles().forEach { child ->
            val childPath = "$basePath${child.name}/"
            if (child.isDirectory) {
                val dirEntry = ZipEntry(childPath)
                zos.putNextEntry(dirEntry)
                zos.closeEntry()
                zipDocumentFile(child, zos, childPath)
            } else {
                val entryName = "$basePath${child.name}"
                val entry = ZipEntry(entryName)
                zos.putNextEntry(entry)
                try {
                    context.contentResolver.openInputStream(child.uri)?.use { fis ->
                        fis.copyTo(zos, 65536)
                    } ?: Log.w(TAG, "zipFolder: openInputStream returned null for ${child.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "zipFolder: error reading ${child.name}", e)
                }
                zos.closeEntry()
            }
        }
    }

    private fun serveStream(session: IHTTPSession, uri: String, ip: String): Response {
        val fileId = extractFileId(uri) ?: return notFound()
        val file = findFile(fileId) ?: return notFound()

        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.STREAM))
        // Note: streaming notifications are omitted to avoid spam (streaming is continuous)

        // Force correct MIME type for known video/audio extensions
        val mimeType = resolveStreamMimeType(file.name, file.mimeType)

        val rangeHeader = session.headers?.get("range")

        // Try file path first for efficient range requests
        val physicalFile = File(file.path)
        return if (physicalFile.exists() && physicalFile.canRead()) {
            RangeRequestHandler.createResponse(physicalFile, mimeType, rangeHeader)
        } else {
            // Fallback to content resolver
            try {
                val inputStream = context.contentResolver.openInputStream(file.uri)
                    ?: return notFound()
                RangeRequestHandler.createResponseFromStream(
                    inputStream, file.size, mimeType, rangeHeader
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error streaming file: ${file.name}", e)
                notFound()
            }
        }
    }

    /**
     * Resolve the correct MIME type based on file extension.
     * Android's MediaStore sometimes returns application/octet-stream
     * for video files, which prevents browser <video> playback.
     */
    private fun resolveStreamMimeType(fileName: String, fallback: String): String {
        if (fallback.startsWith("video/") || fallback.startsWith("audio/") || fallback.startsWith("image/")) {
            return fallback
        }
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".mp4") -> "video/mp4"
            lower.endsWith(".webm") -> "video/webm"
            lower.endsWith(".mkv") -> "video/x-matroska"
            lower.endsWith(".mov") -> "video/quicktime"
            lower.endsWith(".avi") -> "video/x-msvideo"
            lower.endsWith(".m4v") -> "video/mp4"
            lower.endsWith(".ts") -> "video/mp2t"
            lower.endsWith(".3gp") -> "video/3gpp"
            lower.endsWith(".flv") -> "video/x-flv"
            lower.endsWith(".wmv") -> "video/x-ms-wmv"
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".m4a") -> "audio/mp4"
            lower.endsWith(".ogg") -> "audio/ogg"
            lower.endsWith(".wav") -> "audio/wav"
            lower.endsWith(".flac") -> "audio/flac"
            lower.endsWith(".aac") -> "audio/aac"
            lower.endsWith(".wma") -> "audio/x-ms-wma"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".bmp") -> "image/bmp"
            lower.endsWith(".svg") -> "image/svg+xml"
            else -> fallback
        }
    }

    private fun serveFavicon(): Response {
        // Simple SVG favicon
        val svg = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
            <rect width="100" height="100" rx="20" fill="#00BFA5"/>
            <text x="50" y="65" font-size="50" text-anchor="middle" fill="white">📡</text>
        </svg>""".trimIndent()
        return newFixedLengthResponse(Response.Status.OK, "image/svg+xml", svg)
    }

    private fun serveLogo(): Response {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.logo)
            try {
                val available = inputStream.available().toLong()
                RangeRequestHandler.createResponseFromStream(inputStream, available, "image/png", null)
            } catch (e: Exception) {
                inputStream.close()
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving logo", e)
            notFound()
        }
    }

    private fun serveLogoDark(): Response {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.logo_dark)
            newChunkedResponse(Response.Status.OK, "image/png", inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error serving dark logo", e)
            notFound()
        }
    }

    /**
     * Returns the session encryption key only to clients that already passed the PIN gate.
     * Prefer this over embedding the key in the public share URL / QR code.
     */
    private fun serveEncryptionKey(): Response {
        val key = if (encryptionEnabled) getEncryptionKeyBase64() else null
        val json = JSONObject().apply {
            put("encrypted", encryptionEnabled && key != null)
            if (key != null) put("key", key)
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private fun extractFileId(uri: String): Long? {
        return try {
            uri.substringAfterLast("/").toLong()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun findFile(fileId: Long): SharedFile? {
        return getCachedFiles().find { it.id == fileId }
    }

    private fun notFound(): Response {
        return newFixedLengthResponse(
            Response.Status.NOT_FOUND,
            MIME_PLAINTEXT,
            "File not found"
        )
    }

    /**
     * Remove IPs that haven't made a request in the last 5 minutes.
     */
    private fun cleanupStaleConnections() {
        val cutoff = System.currentTimeMillis() - 5 * 60 * 1000
        connectedIps.entries.removeAll { it.value < cutoff }
    }

    private fun serveThumbnail(@Suppress("UNUSED_PARAMETER") session: IHTTPSession, uri: String): Response {
        val fileId = extractFileId(uri) ?: return notFound()
        val file = findFile(fileId) ?: return notFound()

        val cached = thumbnailCache.get(fileId)
        if (cached != null) {
            return newFixedLengthResponse(Response.Status.OK, "image/jpeg", ByteArrayInputStream(cached), cached.size.toLong())
        }

        val bitmap = try {
            when {
                file.mimeType.startsWith("image/") -> {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    if (!file.path.startsWith("virtual://") && File(file.path).exists()) {
                        FileInputStream(File(file.path)).use {
                            BitmapFactory.decodeStream(it, null, options)
                        }
                        options.inSampleSize = calculateInSampleSize(options, 256, 256)
                        options.inJustDecodeBounds = false
                        FileInputStream(File(file.path)).use {
                            BitmapFactory.decodeStream(it, null, options)
                        }
                    } else {
                        context.contentResolver.openInputStream(file.uri)?.use {
                            BitmapFactory.decodeStream(it, null, options)
                        }
                        options.inSampleSize = calculateInSampleSize(options, 256, 256)
                        options.inJustDecodeBounds = false
                        context.contentResolver.openInputStream(file.uri)?.use {
                            BitmapFactory.decodeStream(it, null, options)
                        }
                    }
                }
                file.mimeType.startsWith("video/") -> {
                    val retriever = MediaMetadataRetriever()
                    val physicalFile = File(file.path)
                    if (physicalFile.exists() && physicalFile.canRead()) {
                        retriever.setDataSource(physicalFile.absolutePath)
                    } else {
                        context.contentResolver.openFileDescriptor(file.uri, "r")?.use { fd ->
                            retriever.setDataSource(fd.fileDescriptor)
                        }
                    }
                    val frame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    retriever.release()
                    frame
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate thumbnail for ${file.name}", e)
            null
        }

        if (bitmap == null) {
            return notFound()
        }

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos)
        bitmap.recycle()
        val bytes = bos.toByteArray()
        thumbnailCache.put(fileId, bytes)

        return newFixedLengthResponse(Response.Status.OK, "image/jpeg", ByteArrayInputStream(bytes), bytes.size.toLong())
    }

    private fun serveAppIcon(@Suppress("UNUSED_PARAMETER") session: IHTTPSession, uri: String): Response {
        val fileId = extractFileId(uri) ?: return notFound()
        val file = findFile(fileId) ?: return notFound()
        
        if (file.category != FileCategory.APPS) return notFound()

        try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(file.path, 0)
            
            val drawable = if (packageInfo != null) {
                packageInfo.applicationInfo?.sourceDir = file.path
                packageInfo.applicationInfo?.publicSourceDir = file.path
                packageInfo.applicationInfo?.loadIcon(pm)
            } else {
                androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            }
            
            if (drawable != null) {
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                
                val bos = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bos)
                val bytes = bos.toByteArray()
                return newFixedLengthResponse(Response.Status.OK, "image/png", java.io.ByteArrayInputStream(bytes), bytes.size.toLong())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating app icon for ${file.name}", e)
        }
        return notFound()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun serveZip(session: IHTTPSession, ip: String): Response {
        val idString = session.parameters["ids"]?.firstOrNull() ?: return notFound()
        val ids = idString.split(",").mapNotNull { it.toLongOrNull() }
        val filesToZip = ids.mapNotNull { findFile(it) }

        if (filesToZip.isEmpty()) return notFound()

        val currentKey = encryptionKey
        val shouldEncrypt = encryptionEnabled && currentKey != null

        if (shouldEncrypt) {
            // Buffer the entire ZIP, encrypt, then serve as fixed response
            val baos = ByteArrayOutputStream()
            ZipOutputStream(baos).use { zos ->
                val nameCounts = mutableMapOf<String, Int>()
                for (file in filesToZip) {
                    val count = nameCounts.getOrDefault(file.name, 0)
                    val finalName = if (count > 0) {
                        val dotIndex = file.name.lastIndexOf('.')
                        if (dotIndex != -1) {
                            "${file.name.substring(0, dotIndex)} ($count)${file.name.substring(dotIndex)}"
                        } else {
                            "${file.name} ($count)"
                        }
                    } else {
                        file.name
                    }
                    nameCounts[file.name] = count + 1

                    val entry = ZipEntry(finalName)
                    zos.putNextEntry(entry)
                    context.contentResolver.openInputStream(file.uri)?.use { fis ->
                        fis.copyTo(zos, 65536)
                    }
                    zos.closeEntry()
                    accessLog.add(AccessLogEntry(ip, file.name, AccessAction.DOWNLOAD))
                }
            }
            val zipBytes = baos.toByteArray()
            val encryptedBytes = FileEncryption.encrypt(zipBytes, currentKey!!)
            val res = newFixedLengthResponse(
                Response.Status.OK, "application/octet-stream",
                ByteArrayInputStream(encryptedBytes), encryptedBytes.size.toLong()
            )
            res.addHeader("Content-Disposition", "attachment; filename=\"LocalShare.zip\"")
            res.addHeader("X-Encrypted", "aes-256-gcm")
            res.addHeader("Access-Control-Allow-Origin", "*")
            return res
        } else {
            // Stream ZIP without encryption
            val pos = PipedOutputStream()
            val pis = PipedInputStream(pos, 65536)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ZipOutputStream(pos).use { zos ->
                        val nameCounts = mutableMapOf<String, Int>()
                        for (file in filesToZip) {
                            val count = nameCounts.getOrDefault(file.name, 0)
                            val finalName = if (count > 0) {
                                val dotIndex = file.name.lastIndexOf('.')
                                if (dotIndex != -1) {
                                    "${file.name.substring(0, dotIndex)} ($count)${file.name.substring(dotIndex)}"
                                } else {
                                    "${file.name} ($count)"
                                }
                            } else {
                                file.name
                            }
                            nameCounts[file.name] = count + 1

                            val entry = ZipEntry(finalName)
                            zos.putNextEntry(entry)
                            context.contentResolver.openInputStream(file.uri)?.use { fis ->
                                fis.copyTo(zos, 65536)
                            }
                            zos.closeEntry()
        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.DOWNLOAD))
        notifyFileAccess(file.name, "downloaded", ip)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating zip", e)
                } finally {
                    try { pos.close() } catch (_: Exception) {}
                }
            }

            val res = newChunkedResponse(Response.Status.OK, "application/zip", pis)
            res.addHeader("Content-Disposition", "attachment; filename=\"LocalShare.zip\"")
            res.addHeader("Access-Control-Allow-Origin", "*")
            return res
        }
    }

    private fun notifyFileAccess(fileName: String, action: String, fromIp: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "localshare_access"

            // Create channel if it doesn't exist
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(channelId) == null) {
                    val channel = android.app.NotificationChannel(
                        channelId, "File Access",
                        android.app.NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Notifications when files are accessed"
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }

            val notification = android.app.Notification.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("File $action")
                .setContentText("$fileName — from $fromIp")
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send access notification", e)
        }
    }

    fun resetConnections() {
        connectedIps.clear()
        authenticatedIps.clear()
        _connectedDeviceCount.value = 0
    }

    private fun serveSSE(): Response {
        val pos = PipedOutputStream()
        val pis = PipedInputStream(pos)

        CoroutineScope(Dispatchers.IO).launch {
            val writer = pos.writer()
            try {
                // Send initial connection event
                writer.write("data: {\"type\":\"CONNECTED\"}\n\n")
                writer.write(": " + " ".repeat(8192) + "\n\n")
                writer.flush()

                _serverEvents.collect { event ->
                    writer.write("data: $event\n\n")
                    writer.write(": " + " ".repeat(8192) + "\n\n")
                    writer.flush()
                }
            } catch (e: Exception) {
                // Connection closed by client
            } finally {
                try { writer.close() } catch (e: Exception) {}
            }
        }

        val res = newChunkedResponse(Response.Status.OK, "text/event-stream", pis)
        res.addHeader("Cache-Control", "no-cache")
        res.addHeader("Connection", "keep-alive")
        res.addHeader("Access-Control-Allow-Origin", "*")
        return res
    }

    fun broadcastEvent(eventJson: String) {
        _serverEvents.tryEmit(eventJson)
    }

    private inner class ProgressTrackingInputStream(
        val source: java.io.InputStream,
        val ip: String,
        val filename: String,
        val fileId: Long,
        val totalBytes: Long
    ) : java.io.InputStream() {
        private var bytesRead = 0L
        private var lastUpdateTime = System.currentTimeMillis()
        private var lastBytesRead = 0L

        init {
            updateFlow(0f, 0L)
        }

        override fun read(): Int {
            val byte = source.read()
            if (byte != -1) track(1) else complete()
            return byte
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val read = source.read(b, off, len)
            if (read != -1) track(read) else complete()
            return read
        }

        override fun close() {
            complete()
            source.close()
        }

        private fun track(bytes: Int) {
            bytesRead += bytes
            val now = System.currentTimeMillis()
            if (now - lastUpdateTime >= 500) {
                val timeDiff = (now - lastUpdateTime) / 1000.0
                val speed = if (timeDiff > 0) ((bytesRead - lastBytesRead) / timeDiff).toLong() else 0L
                val progress = if (totalBytes > 0) (bytesRead.toFloat() / totalBytes) else 0f
                updateFlow(progress, speed)
                lastUpdateTime = now
                lastBytesRead = bytesRead
            }
        }

        private fun updateFlow(progress: Float, speed: Long) {
            val key = "$ip-$fileId"
            activeDownloadsMap[key] = ActiveDownload(ip, filename, progress, speed, fileId)
            _activeDownloads.value = activeDownloadsMap.values.toList()
        }

        private fun complete() {
            val key = "$ip-$fileId"
            activeDownloadsMap.remove(key)
            _activeDownloads.value = activeDownloadsMap.values.toList()
        }
    }
}
