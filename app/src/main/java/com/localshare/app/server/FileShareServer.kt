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
 *   GET /download/{fileId} → Full file download
 *   GET /stream/{fileId}   → Stream with Range request support
 */
class FileShareServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    companion object {
        private const val TAG = "FileShareServer"
    }

    private val fileRepository = FileRepository(context)
    val accessLog = AccessLogBuffer()

    private val connectedIps = ConcurrentHashMap<String, Long>()
    private val _connectedDeviceCount = MutableStateFlow(0)
    val connectedDeviceCount: StateFlow<Int> = _connectedDeviceCount.asStateFlow()

    private val thumbnailCache = object : LruCache<Long, ByteArray>(20 * 1024 * 1024) { // 20MB cache
        override fun sizeOf(key: Long, value: ByteArray): Int = value.size
    }

    // Authenticated IPs (have entered the correct PIN)
    private val authenticatedIps = ConcurrentHashMap<String, Long>()

    @Volatile
    var shareConfig: ShareConfig = ShareConfig()

    // Server settings — updated from app settings
    @Volatile
    var pin: String? = null

    @Volatile
    var deviceName: String = "LocalShare"

    @Volatile
    var maxConnections: Int = 3

    // ─── File list cache (avoids runBlocking deadlock under load) ────
    @Volatile
    private var cachedFiles: List<SharedFile> = emptyList()
    @Volatile
    private var cacheTime: Long = 0

    private fun getCachedFiles(): List<SharedFile> {
        val now = System.currentTimeMillis()
        if (now - cacheTime > 3000) { // refresh every 3 seconds
            cachedFiles = runBlocking { fileRepository.getSharedFiles(shareConfig) }
            cacheTime = now
        }
        return cachedFiles
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        val ip = session.remoteIpAddress ?: "unknown"
        val method = session.method

        // Track connected device
        connectedIps[ip] = System.currentTimeMillis()
        cleanupStaleConnections()
        _connectedDeviceCount.value = connectedIps.size

        Log.d(TAG, "$method $uri from $ip")

        return try {
            // ─── Auth endpoint is always accessible ─────────────
            if (uri == "/api/auth" && method == Method.POST) {
                return handleAuth(session, ip)
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
                uri == "/api/status" -> serveStatus()
                uri.startsWith("/download/") -> serveDownload(session, uri, ip)
                uri.startsWith("/stream/") -> serveStream(session, uri, ip)
                uri.startsWith("/api/thumbnail/") -> serveThumbnail(session, uri)
                uri.startsWith("/api/icon/") -> serveAppIcon(session, uri)
                uri == "/api/download-zip" -> serveZip(session, ip)
                uri == "/favicon.ico" -> serveFavicon()
                uri == "/logo.png" -> serveLogo()
                uri == "/logo-dark.png" -> serveLogoDark()
                else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error serving $uri", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Internal Server Error: ${e.message}"
            )
        }
    }

    // ─── Auth Handler ───────────────────────────────────────────

    private fun handleAuth(session: IHTTPSession, ip: String): Response {
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
            // Authenticate this IP
            authenticatedIps[ip] = System.currentTimeMillis()
            newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                """{"success":true,"deviceName":"$deviceName"}"""
            )
        } else {
            newFixedLengthResponse(
                Response.Status.UNAUTHORIZED,
                "application/json",
                """{"success":false,"error":"Incorrect PIN"}"""
            )
        }
    }

    private fun isAuthenticated(ip: String): Boolean {
        val authTime = authenticatedIps[ip] ?: return false
        // Auth expires after 24 hours
        return System.currentTimeMillis() - authTime < 24 * 60 * 60 * 1000
    }

    // ─── Route Handlers ─────────────────────────────────────────

    private fun serveWebUI(): Response {
        val needsAuth = pin != null
        val html = WebUI.getHtml(deviceName, needsAuth)
        return newFixedLengthResponse(Response.Status.OK, "text/html", html).also {
            it.addHeader("Cache-Control", "no-cache")
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
            val obj = JSONObject().apply {
                put("id", file.id)
                put("name", file.name)
                put("size", file.size)
                put("formattedSize", file.formattedSize)
                put("mimeType", file.mimeType)
                put("category", file.category.name.lowercase())
                put("typeIcon", file.typeIcon)
                put("isStreamable", file.isStreamable)
                put("lastModified", file.lastModified)
            }
            jsonArray.put(obj)
        }

        val responseJson = JSONObject().apply {
            put("files", jsonArray)
            put("count", filtered.size)
            put("connectedDevices", connectedIps.size)
            put("pinProtected", pin != null)
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
            put("sharedFileCount", getCachedFiles().size)
        }

        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        )
    }

    private fun serveDownload(@Suppress("UNUSED_PARAMETER") session: IHTTPSession, uri: String, ip: String): Response {
        val fileId = extractFileId(uri) ?: return notFound()
        val file = findFile(fileId) ?: return notFound()

        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.DOWNLOAD))

        // Try file path first, then content URI
        val physicalFile = File(file.path)
        return if (physicalFile.exists() && physicalFile.canRead()) {
            val bounded = BoundedInputStream(FileInputStream(physicalFile), physicalFile.length())
            val response = newFixedLengthResponse(
                Response.Status.OK,
                file.mimeType,
                bounded,
                physicalFile.length()
            )
            response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
            response
        } else {
            // Fallback to content resolver
            try {
                val inputStream = context.contentResolver.openInputStream(file.uri)
                    ?: return notFound()
                val bounded = BoundedInputStream(inputStream, file.size)
                val response = newFixedLengthResponse(
                    Response.Status.OK,
                    file.mimeType,
                    bounded,
                    file.size
                )
                response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
                response
            } catch (e: Exception) {
                Log.e(TAG, "Error opening file for download: ${file.name}", e)
                notFound()
            }
        }
    }

    private fun serveStream(session: IHTTPSession, uri: String, ip: String): Response {
        val fileId = extractFileId(uri) ?: return notFound()
        val file = findFile(fileId) ?: return notFound()

        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.STREAM))

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
            lower.endsWith(".mp3") -> "audio/mpeg"
            lower.endsWith(".m4a") -> "audio/mp4"
            lower.endsWith(".ogg") -> "audio/ogg"
            lower.endsWith(".wav") -> "audio/wav"
            lower.endsWith(".flac") -> "audio/flac"
            lower.endsWith(".aac") -> "audio/aac"
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
            val inputStream = context.resources.openRawResource(R.drawable.logo)
            val available = inputStream.available().toLong()
            RangeRequestHandler.createResponseFromStream(inputStream, available, "image/png", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error serving logo", e)
            notFound()
        }
    }

    private fun serveLogoDark(): Response {
        return try {
            val inputStream = context.resources.openRawResource(R.drawable.logo_dark)
            newChunkedResponse(Response.Status.OK, "image/png", inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error serving dark logo", e)
            notFound()
        }
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
                    context.contentResolver.openInputStream(file.uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                    options.inSampleSize = calculateInSampleSize(options, 256, 256)
                    options.inJustDecodeBounds = false
                    context.contentResolver.openInputStream(file.uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
                file.mimeType.startsWith("video/") -> {
                    val retriever = MediaMetadataRetriever()
                    context.contentResolver.openFileDescriptor(file.uri, "r")?.use { fd ->
                        retriever.setDataSource(fd.fileDescriptor)
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
                packageInfo.applicationInfo.sourceDir = file.path
                packageInfo.applicationInfo.publicSourceDir = file.path
                packageInfo.applicationInfo.loadIcon(pm)
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

        val pos = PipedOutputStream()
        val pis = PipedInputStream(pos, 65536)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ZipOutputStream(pos).use { zos ->
                    for (file in filesToZip) {
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        context.contentResolver.openInputStream(file.uri)?.use { fis ->
                            fis.copyTo(zos, 65536)
                        }
                        zos.closeEntry()
                        accessLog.add(AccessLogEntry(ip, file.name, AccessAction.DOWNLOAD))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating zip", e)
            }
        }

        val res = newChunkedResponse(Response.Status.OK, "application/zip", pis)
        res.addHeader("Content-Disposition", "attachment; filename=\"LocalShare.zip\"")
        res.addHeader("Access-Control-Allow-Origin", "*")
        return res
    }

    fun resetConnections() {
        connectedIps.clear()
        authenticatedIps.clear()
        _connectedDeviceCount.value = 0
    }
}
