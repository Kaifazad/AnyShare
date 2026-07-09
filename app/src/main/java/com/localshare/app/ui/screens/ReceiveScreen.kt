package com.localshare.app.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RemoteFile(
    val id: String,
    val name: String,
    val size: String,
    val type: String,
    val downloadUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveBottomSheet(initialUrl: String? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    var serverUrl by remember { mutableStateOf(initialUrl ?: "") }
    var remoteFiles by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    // Download progress tracking: fileId -> (progress 0..100, speedBytes/s, etaSeconds)
    var downloadProgress by remember { mutableStateOf<Map<String, Triple<Float, Long, Long>>>(emptyMap()) }

    val connectToServer = { urlToConnect: String ->
        val url = if (urlToConnect.endsWith("/")) urlToConnect.dropLast(1) else urlToConnect
        val finalUrl = if (!url.startsWith("http")) "http://$url" else url
        serverUrl = finalUrl
        
        isLoading = true
        fetchFiles(finalUrl) { files, error ->
            isLoading = false
            if (error == "401") {
                showPinDialog = true
            } else if (error != null) {
                errorMsg = error
                isConnected = false
            } else {
                remoteFiles = files
                isConnected = true
                errorMsg = null
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrEmpty()) {
            connectToServer(initialUrl)
        }
    }

    // Periodically refresh file list while connected
    androidx.compose.runtime.LaunchedEffect(isConnected, serverUrl) {
        if (isConnected && serverUrl.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                fetchFiles(serverUrl) { files, error ->
                    if (error == null) {
                        remoteFiles = files
                    }
                }
            }
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            connectToServer(result.contents)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isConnected) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Connect to Sender",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Scan the QR code on the sender's device or enter their IP address.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Scan Sender's QR Code")
                        options.setBeepEnabled(true)
                        options.setCaptureActivity(com.localshare.app.ui.components.PortraitCaptureActivity::class.java)
                        options.setOrientationLocked(true)
                        scanLauncher.launch(options)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL (e.g. http://192.168.1.5:8080)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        connectToServer(serverUrl)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Connect")
                    }
                }
                
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                }

                if (showPinDialog) {
                    AlertDialog(
                        onDismissRequest = { showPinDialog = false },
                        title = { Text("PIN Required") },
                        text = {
                            Column {
                                Text("Enter the 4-digit PIN to connect.")
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { pinInput = it },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showPinDialog = false
                                isLoading = true
                                authenticateAndFetch(serverUrl, pinInput) { files, error ->
                                    isLoading = false
                                    if (error != null) {
                                        errorMsg = error
                                        isConnected = false
                                    } else {
                                        remoteFiles = files
                                        isConnected = true
                                        errorMsg = null
                                    }
                                }
                            }) {
                                Text("Submit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPinDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
            } else {
                // Connected State
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Connected to", style = MaterialTheme.typography.bodySmall)
                            Text(serverUrl, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { isConnected = false; serverUrl = "" }) {
                            Text("Disconnect")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (remoteFiles.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No files shared", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(remoteFiles.size) { index ->
                            val file = remoteFiles[index]
                            val progress = downloadProgress[file.id]
                            val isDownloading = progress != null

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = file.name,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (isDownloading) {
                                                val speedKb = progress!!.second / 1024
                                                val speedStr = if (speedKb > 1024) String.format("%.1f MB/s", speedKb / 1024.0) else "$speedKb KB/s"
                                                val etaStr = if (progress.third > 0) "${progress.third}s left" else ""
                                                Text(
                                                    text = "${progress.first.toInt()}% · $speedStr $etaStr",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Text(
                                                    text = file.size,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        
                                        val interactionSource = remember { MutableInteractionSource() }
                                        if (isDownloading) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                                                CircularProgressIndicator(
                                                    progress = { progress!!.first / 100f },
                                                    modifier = Modifier.size(48.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                    strokeWidth = 4.dp
                                                )
                                                Icon(
                                                    Icons.Filled.Download,
                                                    contentDescription = "Downloading",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick = {
                                                    downloadFile(context, file.downloadUrl, file.name) { prog, speed, eta ->
                                                        downloadProgress = downloadProgress + (file.id to Triple(prog, speed, eta))
                                                        if (prog >= 100f) {
                                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                                                kotlinx.coroutines.delay(500)
                                                                downloadProgress = downloadProgress - file.id
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.bounceScale(interactionSource),
                                                interactionSource = interactionSource,
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Download,
                                                    contentDescription = "Download",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun fetchFiles(baseUrl: String, onResult: (List<RemoteFile>, String?) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$baseUrl/api/files")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                val jsonArray = jsonObject.getJSONArray("files")
                val files = mutableListOf<RemoteFile>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    files.add(
                        RemoteFile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            size = obj.getString("formattedSize"),
                            type = obj.getString("typeIcon"),
                            downloadUrl = "$baseUrl/download/${obj.getString("id")}"
                        )
                    )
                }
                withContext(Dispatchers.Main) { onResult(files, null) }
            } else if (connection.responseCode == 401) {
                withContext(Dispatchers.Main) { onResult(emptyList(), "401") }
            } else {
                withContext(Dispatchers.Main) { onResult(emptyList(), "Server returned ${connection.responseCode}") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(emptyList(), "Failed to connect: ${e.localizedMessage}") }
        } finally {
            connection?.disconnect()
        }
    }
}

private fun authenticateAndFetch(baseUrl: String, pin: String, onResult: (List<RemoteFile>, String?) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$baseUrl/api/auth")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val jsonBody = JSONObject().apply { put("pin", pin) }.toString()
            connection.outputStream.write(jsonBody.toByteArray())

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                fetchFiles(baseUrl, onResult)
            } else if (responseCode == 429) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: ""
                val retryAfter = try {
                    JSONObject(errorBody).optInt("retryAfter", 30)
                } catch (e: Exception) { 30 }
                withContext(Dispatchers.Main) { onResult(emptyList(), "Locked. Try again in ${retryAfter}s") }
            } else {
                withContext(Dispatchers.Main) { onResult(emptyList(), "Incorrect PIN or Server Error") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(emptyList(), "Failed to connect: ${e.localizedMessage}") }
        } finally {
            connection?.disconnect()
        }
    }
}

private fun downloadFile(
    context: Context,
    url: String,
    fileName: String,
    onProgress: ((progress: Float, speedBytesPerSec: Long, etaSeconds: Long) -> Unit)? = null
) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        var connection: HttpURLConnection? = null
        try {
            // Extract encryption key from URL if present
            val parsedUrl = java.net.URL(url)
            val keyParam = parsedUrl.query?.split("&")?.find { it.startsWith("key=") }?.removePrefix("key=")
            val encryptionKey = keyParam?.let { com.localshare.app.server.FileEncryption.decodeKey(it) }

            // Determine target directory for resume check
            val subFolder = when {
                fileName.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|bmp|svg)$", RegexOption.IGNORE_CASE)) -> "Photos"
                fileName.matches(Regex(".*\\.(mp4|mkv|mov|avi|webm|m4v|3gp|flv|wmv)$", RegexOption.IGNORE_CASE)) -> "Videos"
                fileName.matches(Regex(".*\\.(mp3|m4a|ogg|wav|flac|aac|wma)$", RegexOption.IGNORE_CASE)) -> "Audio"
                else -> "Documents"
            }
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val targetDir = java.io.File(downloadsDir, "LocalShare/$subFolder")
            if (!targetDir.exists()) targetDir.mkdirs()

            // Check for partial file to resume
            val partialFile = java.io.File(targetDir, "$fileName.part")
            var resumeFrom = 0L
            if (partialFile.exists() && encryptionKey == null) {
                resumeFrom = partialFile.length()
            }

            connection = parsedUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 60000

            // Add Range header for resumable download
            if (resumeFrom > 0) {
                connection.setRequestProperty("Range", "bytes=$resumeFrom-")
            }

            val responseCode = connection.responseCode
            val isResuming = responseCode == 206
            val isFullDownload = responseCode == 200

            if (isResuming || isFullDownload) {
                val contentLength = connection.contentLength.toLong()
                val totalSize = if (isResuming) resumeFrom + contentLength else contentLength
                val inputStream = connection.inputStream

                // If resuming, load existing partial bytes
                val baos = java.io.ByteArrayOutputStream()
                if (isResuming && partialFile.exists()) {
                    baos.write(partialFile.readBytes())
                }

                // Read with progress tracking
                val buffer = ByteArray(8192)
                var totalRead = 0L
                val startTime = System.currentTimeMillis()
                var lastReportTime = startTime

                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    baos.write(buffer, 0, read)
                    totalRead += read

                    // Report progress every 200ms
                    val now = System.currentTimeMillis()
                    if (now - lastReportTime >= 200) {
                        val elapsed = (now - startTime).coerceAtLeast(1)
                        val speed = (resumeFrom + totalRead) * 1000 / (elapsed + (if (isResuming) 1 else 0))
                        val progress = if (totalSize > 0) ((resumeFrom + totalRead) * 100f / totalSize) else 0f
                        val remaining = totalSize - resumeFrom - totalRead
                        val eta = if (speed > 0) remaining / speed else 0
                        withContext(Dispatchers.Main) {
                            onProgress?.invoke(progress.coerceAtMost(100f), speed, eta)
                        }
                        lastReportTime = now

                        // Save partial file for resume capability (every 500KB)
                        if (totalRead % 512000 < 8192 && encryptionKey == null) {
                            partialFile.writeBytes(baos.toByteArray())
                        }
                    }
                }
                inputStream.close()

                val bytes = baos.toByteArray()

                // Clean up partial file
                if (partialFile.exists()) partialFile.delete()

                // Decrypt if encryption key was present
                val finalBytes = if (encryptionKey != null) {
                    try {
                        com.localshare.app.server.FileEncryption.decrypt(bytes, encryptionKey)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Decryption failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }
                } else {
                    bytes
                }

                val outputFile = java.io.File(targetDir, fileName)
                outputFile.writeBytes(finalBytes)

                // Scan the file so it appears in gallery/file managers
                android.media.MediaScannerConnection.scanFile(context, arrayOf(outputFile.absolutePath), null, null)

                withContext(Dispatchers.Main) {
                    onProgress?.invoke(100f, 0, 0)
                    com.localshare.app.ui.utils.HapticHelper.performTransferComplete(context)
                    Toast.makeText(context, "Downloaded $fileName", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download failed: HTTP $responseCode", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } finally {
            connection?.disconnect()
        }
    }
}
