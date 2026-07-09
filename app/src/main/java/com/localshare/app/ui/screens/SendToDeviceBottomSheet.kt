package com.localshare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.utils.bounceScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DiscoveredDevice(
    val name: String,
    val ip: String,
    val port: Int,
    val protocol: String = "http"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendToDeviceBottomSheet(
    onDismiss: () -> Unit,
    viewModel: FileShareViewModel
) {
    val context = LocalContext.current
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles
    val scope = rememberCoroutineScope()

    var discoveredDevices by remember { mutableStateOf<List<DiscoveredDevice>>(emptyList()) }
    var isScanning by remember { mutableStateOf(true) }
    var selectedDevice by remember { mutableStateOf<DiscoveredDevice?>(null) }
    var isSending by remember { mutableStateOf(false) }
    var sendStatus by remember { mutableStateOf<String?>(null) }

    // Scan for nearby devices
    LaunchedEffect(Unit) {
        isScanning = true
        discoveredDevices = scanForDevices()
        isScanning = false
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Send to Device",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${sharedFiles.size} files ready to send",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Send status
            sendStatus?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.contains("Success"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (status.contains("Success")) Icons.Filled.CheckCircle else Icons.Filled.SignalWifiOff,
                            contentDescription = null,
                            tint = if (status.contains("Success"))
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Device list
            Text(
                text = "Nearby Devices",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Scanning for devices...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (discoveredDevices.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.SignalWifiOff,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No devices found nearby",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = {
                            scope.launch {
                                isScanning = true
                                discoveredDevices = scanForDevices()
                                isScanning = false
                            }
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(discoveredDevices) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = selectedDevice?.ip == device.ip,
                            onClick = { selectedDevice = device }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Send button
            Button(
                onClick = {
                    val device = selectedDevice ?: return@Button
                    isSending = true
                    scope.launch {
                        val result = sendFilesToDevice(
                            context = context,
                            device = device,
                            files = sharedFiles,
                            senderName = viewModel.appSettings.value.deviceName
                        )
                        isSending = false
                        sendStatus = result
                        if (result.contains("Success")) {
                            delay(2000)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedDevice != null && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sending...")
                } else {
                    Text(
                        text = if (selectedDevice != null) "Send to ${selectedDevice!!.name}" else "Select a device",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: DiscoveredDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceScale(interactionSource),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Filled.PhoneAndroid,
                        contentDescription = null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${device.ip}:${device.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Scan for nearby LocalShare devices using UDP multicast.
 */
private suspend fun scanForDevices(): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
    val devices = mutableListOf<DiscoveredDevice>()
    try {
        val socket = java.net.MulticastSocket(53318)
        socket.soTimeout = 2000
        val group = java.net.InetAddress.getByName("224.0.0.167")
        val packet = java.net.DatagramPacket(ByteArray(1024), 1024, group, 53317)

        // Send discovery request
        val requestData = """{"app":"LocalShare","type":"discover"}""".toByteArray()
        val sendPacket = java.net.DatagramPacket(requestData, requestData.size, group, 53317)
        socket.send(sendPacket)

        // Listen for responses
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < 2000) {
            try {
                socket.receive(packet)
                val response = String(packet.data, 0, packet.length)
                val json = JSONObject(response)
                if (json.optString("app") == "LocalShare") {
                    val ip = packet.address.hostAddress ?: continue
                    // Don't add self
                    if (ip != getLocalIp()) {
                        devices.add(
                            DiscoveredDevice(
                                name = json.optString("name", "Unknown"),
                                ip = ip,
                                port = json.optInt("port", 8080)
                            )
                        )
                    }
                }
            } catch (_: java.net.SocketTimeoutException) {
                break
            }
        }
        socket.close()
    } catch (e: Exception) {
        // Fallback: try common local IPs
    }
    devices
}

private fun getLocalIp(): String? {
    try {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                    return address.hostAddress
                }
            }
        }
    } catch (_: Exception) {}
    return null
}

private suspend fun sendFilesToDevice(
    context: android.content.Context,
    device: DiscoveredDevice,
    files: List<com.localshare.app.data.SharedFile>,
    senderName: String
): String = withContext(Dispatchers.IO) {
    try {
        val sessionId = "session_${System.currentTimeMillis()}_to_${device.ip.replace(".", "")}"
        val db = com.localshare.app.data.db.AppDatabase.getDatabase(context)
        val sessionDao = db.transferSessionDao()

        var totalSize = 0L
        val fileEntities = files.mapIndexed { index, file ->
            totalSize += file.size
            com.localshare.app.data.db.TransferFileEntity(
                sessionId = sessionId,
                fileId = "file_$index",
                fileName = file.name,
                size = file.size,
                fileType = file.mimeType,
                uriString = file.uri.toString()
            )
        }

        val sessionEntity = com.localshare.app.data.db.TransferSessionEntity(
            sessionId = sessionId,
            senderName = senderName,
            senderIp = device.ip,
            totalFiles = files.size,
            totalSize = totalSize,
            transferredBytes = 0,
            status = com.localshare.app.data.SessionStatus.ACTIVE,
            transferType = com.localshare.app.data.TransferType.SEND,
            startTime = System.currentTimeMillis(),
            endTime = 0
        )

        sessionDao.insertSession(sessionEntity)
        sessionDao.insertFiles(fileEntities)

        val baseUrl = "${device.protocol}://${device.ip}:${device.port}"

        // Prepare upload
        val filesJson = org.json.JSONArray()
        files.forEachIndexed { index, file ->
            filesJson.put(org.json.JSONObject().apply {
                put("id", "file_$index")
                put("fileName", file.name)
                put("size", file.size)
                put("fileType", file.mimeType)
            })
        }

        val prepareBody = org.json.JSONObject().apply {
            put("senderName", senderName)
            put("files", filesJson)
        }

        val prepareUrl = java.net.URL("$baseUrl/api/prepare-upload")
        val prepareConn = prepareUrl.openConnection() as java.net.HttpURLConnection
        prepareConn.requestMethod = "POST"
        prepareConn.setRequestProperty("Content-Type", "application/json")
        prepareConn.doOutput = true
        prepareConn.connectTimeout = 5000
        prepareConn.readTimeout = 10000
        prepareConn.outputStream.write(prepareBody.toString().toByteArray())

        if (prepareConn.responseCode != 200) {
            sessionDao.updateSession(sessionEntity.copy(status = com.localshare.app.data.SessionStatus.FAILED, endTime = System.currentTimeMillis()))
            return@withContext "Failed: Server returned ${prepareConn.responseCode}"
        }

        val prepareResponse = prepareConn.inputStream.bufferedReader().readText()
        val remoteSessionId = org.json.JSONObject(prepareResponse).optString("sessionId", "")
        if (remoteSessionId.isEmpty()) {
            sessionDao.updateSession(sessionEntity.copy(status = com.localshare.app.data.SessionStatus.FAILED, endTime = System.currentTimeMillis()))
            return@withContext "Failed: No session ID"
        }

        // Poll until receiver accepts
        var remoteStatus = "pending"
        var pollCount = 0
        while (remoteStatus == "pending" && pollCount < 30) {
            delay(1000)
            pollCount++
            try {
                val statusUrl = java.net.URL("$baseUrl/api/session/status?sessionId=$remoteSessionId")
                val statusConn = statusUrl.openConnection() as java.net.HttpURLConnection
                statusConn.requestMethod = "GET"
                statusConn.connectTimeout = 3000
                statusConn.readTimeout = 3000
                if (statusConn.responseCode == 200) {
                    val body = statusConn.inputStream.bufferedReader().readText()
                    remoteStatus = org.json.JSONObject(body).optString("status", "pending").lowercase()
                }
            } catch (_: Exception) {}
        }

        if (remoteStatus == "rejected") {
            sessionDao.updateSession(sessionEntity.copy(status = com.localshare.app.data.SessionStatus.REJECTED, endTime = System.currentTimeMillis()))
            return@withContext "Rejected by receiver"
        }
        if (remoteStatus != "active") {
            sessionDao.updateSession(sessionEntity.copy(status = com.localshare.app.data.SessionStatus.FAILED, endTime = System.currentTimeMillis()))
            return@withContext "Receiver did not respond"
        }

        // Upload files
        var totalTransferred = 0L
        for ((index, file) in files.withIndex()) {
            val uri = android.net.Uri.parse(file.uri.toString())
            val uploadUrl = java.net.URL("$baseUrl/api/upload?sessionId=$remoteSessionId&fileId=file_$index&filename=${file.name}")
            val uploadConn = uploadUrl.openConnection() as java.net.HttpURLConnection
            uploadConn.requestMethod = "POST"
            uploadConn.setRequestProperty("Content-Type", "application/octet-stream")
            uploadConn.doOutput = true
            uploadConn.connectTimeout = 10000
            uploadConn.readTimeout = 120000

            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    uploadConn.outputStream.use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
                if (uploadConn.responseCode == 200) {
                    totalTransferred += file.size
                    sessionDao.updateSession(sessionEntity.copy(
                        status = com.localshare.app.data.SessionStatus.ACTIVE,
                        transferredBytes = totalTransferred
                    ))
                }
            } catch (e: Exception) {
                sessionDao.updateSession(sessionEntity.copy(status = com.localshare.app.data.SessionStatus.FAILED, endTime = System.currentTimeMillis()))
                return@withContext "Upload error: ${e.message}"
            }
        }

        sessionDao.updateSession(sessionEntity.copy(
            status = com.localshare.app.data.SessionStatus.COMPLETED,
            transferredBytes = totalTransferred,
            endTime = System.currentTimeMillis()
        ))
        "Success: ${files.size} files sent"
    } catch (e: Exception) {
        "Failed: ${e.message}"
    }
}
