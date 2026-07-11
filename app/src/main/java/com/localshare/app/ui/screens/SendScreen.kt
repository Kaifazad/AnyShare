package com.localshare.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.localshare.app.data.SharedFile
import com.localshare.app.data.db.AppDatabase
import com.localshare.app.data.db.TransferFileEntity
import com.localshare.app.data.db.TransferSessionEntity
import com.localshare.app.server.DiscoveredDevice
import com.localshare.app.server.DiscoveryListener
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.utils.bounceScale
import com.localshare.app.util.NetworkUtils
import com.localshare.app.util.NsdHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class SendViewModel : ViewModel() {
    private var discoveryListener: DiscoveryListener? = null
    
    private val _devices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val devices: StateFlow<List<DiscoveredDevice>> = _devices.asStateFlow()

    fun startDiscovery(context: Context) {
        if (discoveryListener == null) {
            discoveryListener = DiscoveryListener(context).apply { start() }
            NsdHelper.startDiscovery(context)
            
            viewModelScope.launch {
                combine(
                    discoveryListener!!.discoveredDevices,
                    NsdHelper.discoveredDevices
                ) { udpDevices, nsdDevices ->
                    val map = mutableMapOf<String, DiscoveredDevice>()
                    for (d in nsdDevices) map[d.ip] = d
                    for (d in udpDevices) map[d.ip] = d // UDP takes precedence
                    
                    val myIp = NetworkUtils.getLocalIpAddress(context)
                    map.remove(myIp) // Don't show self
                    
                    map.values.toList().sortedByDescending { it.lastSeen }
                }.collect {
                    _devices.value = it
                }
            }
        }
    }

    fun stopDiscovery() {
        discoveryListener?.stop()
        discoveryListener = null
        NsdHelper.stopDiscovery()
        _devices.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
    }
}

@Composable
fun SendScreen(
    viewModel: FileShareViewModel,
    navController: NavController,
    sendViewModel: SendViewModel = viewModel()
) {
    val context = LocalContext.current
    val devices by sendViewModel.devices.collectAsState()
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles
    val scope = rememberCoroutineScope()
    val fileRepository = remember { com.localshare.app.data.FileRepository(context) }
    val isRunning by viewModel.isServerRunning.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var isSending by remember { mutableStateOf(false) }
    var sendingDeviceIp by remember { mutableStateOf<String?>(null) }
    var showTextPasteDialog by remember { mutableStateOf(false) }
    var showAppPickerSheet by remember { mutableStateOf(false) }

    // Start discovery when screen opens
    DisposableEffect(Unit) {
        sendViewModel.startDiscovery(context.applicationContext)
        onDispose {
            sendViewModel.stopDiscovery()
        }
    }

    val filePicker = rememberLauncherForActivityResult(OpenFilesAtRootContract()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
            }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
            }
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val folder = fileRepository.resolveFolder(uri)
                if (folder != null) {
                    viewModel.addSharedFiles(listOf(folder))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        // ─── Server Status Card ────────────────────────────────────────
        val greenColor = Color(0xFF22C55E)
        val redColor = Color(0xFFEF4444)
        val statusColor = if (isRunning) greenColor else redColor

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRunning) "Server Running" else "Server Offline",
                            style = MaterialTheme.typography.titleSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isRunning) "Ready to receive files" else "Tap to start sharing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    FilledTonalButton(
                        onClick = { viewModel.toggleServer() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        interactionSource = interactionSource,
                        modifier = Modifier.bounceScale(interactionSource),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isRunning) redColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isRunning) redColor else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = if (isRunning) "Stop" else "Start",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // Server URL (only when running)
                AnimatedVisibility(visible = isRunning && serverUrl != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = serverUrl ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("URL", serverUrl ?: "")
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "URL copied!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy URL",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Header ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Send Files",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pick files and tap a device to send instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Pickers (All 5 types) ────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Media, Files, Folders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val interaction1 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                FilledTonalButton(
                    onClick = {
                        mediaPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .bounceScale(interaction1),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = interaction1
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Image, contentDescription = "Media")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Media", style = MaterialTheme.typography.labelMedium)
                    }
                }

                val interaction2 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                FilledTonalButton(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .bounceScale(interaction2),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = interaction2
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Rounded.InsertDriveFile, contentDescription = "Files")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Files", style = MaterialTheme.typography.labelMedium)
                    }
                }

                val interaction3 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                FilledTonalButton(
                    onClick = { folderPicker.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .bounceScale(interaction3),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = interaction3
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Folder, contentDescription = "Folders")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Folders", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Row 2: Apps, Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val interaction4 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                FilledTonalButton(
                    onClick = { showAppPickerSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .bounceScale(interaction4),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = interaction4,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.PhoneAndroid, contentDescription = "Apps")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Apps", style = MaterialTheme.typography.labelMedium)
                    }
                }

                val interaction5 = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                FilledTonalButton(
                    onClick = { showTextPasteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .bounceScale(interaction5),
                    shape = RoundedCornerShape(16.dp),
                    interactionSource = interaction5,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Text")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Text", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Spacer to balance
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Selected Files List ──────────────────────────────────────
        AnimatedVisibility(visible = sharedFiles.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected Files (${sharedFiles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.clearSharedFiles() }) {
                        Text("Clear All")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    sharedFiles.forEach { file ->
                        FileChip(
                            file = file,
                            onRemove = { viewModel.removeSharedFile(it) },
                            onClick = { navController.navigate("file_preview/${it}") }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ─── Nearby Devices ───────────────────────────────────────────
        Text(
            text = "Nearby Devices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Searching for nearby devices...",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Make sure the other device is on the same Wi-Fi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                devices.forEach { device ->
                    val isThisDeviceSending = isSending && sendingDeviceIp == device.ip
                    DeviceCard(device = device, isSending = isThisDeviceSending) {
                        if (sharedFiles.isEmpty()) {
                            android.widget.Toast.makeText(context, "Select files first!", android.widget.Toast.LENGTH_SHORT).show()
                            return@DeviceCard
                        }
                        isSending = true
                        sendingDeviceIp = device.ip
                        scope.launch {
                            sendFilesToDevice(
                                context = context,
                                device = device,
                                files = sharedFiles,
                                senderName = viewModel.appSettings.value.deviceName
                            )
                            isSending = false
                            sendingDeviceIp = null
                            android.widget.Toast.makeText(context, "Transfer started! Check History tab.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // ─── Text Share Dialog ──────────────────────────────────────────
    if (showTextPasteDialog) {
        TextShareBottomSheet(
            onDismiss = { showTextPasteDialog = false },
            onShare = { pastedText, createTextFile ->
                if (pastedText.isNotBlank()) {
                    if (createTextFile) {
                        val textId = pastedText.hashCode().toLong()
                        val textFile = com.localshare.app.data.SharedFile(
                            id = textId,
                            name = "SharedText_${System.currentTimeMillis()}.txt",
                            path = "virtual://$pastedText",
                            uri = android.net.Uri.parse("virtual://$textId"),
                            size = pastedText.toByteArray().size.toLong(),
                            mimeType = "text/plain",
                            category = com.localshare.app.data.FileCategory.DOCUMENTS,
                            lastModified = System.currentTimeMillis()
                        )
                        viewModel.addSharedFiles(listOf(textFile))
                    }
                    try {
                        com.localshare.app.service.ServerForegroundService.updateServerClipboard(pastedText)
                        val toastMsg = if (createTextFile) "Shared text file" else "Text shared"
                        android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                showTextPasteDialog = false
            }
        )
    }

    // ─── App Picker Sheet ───────────────────────────────────────────
    if (showAppPickerSheet) {
        AppPickerBottomSheet(
            onDismiss = { showAppPickerSheet = false },
            onAppsSelected = { appFiles ->
                if (appFiles.isNotEmpty()) {
                    viewModel.addSharedFiles(appFiles)
                }
                showAppPickerSheet = false
            }
        )
    }
}

@Composable
fun FileChip(file: SharedFile, onRemove: (Long) -> Unit, onClick: (Long) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick(file.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isMedia = file.category == com.localshare.app.data.FileCategory.PHOTOS || file.category == com.localshare.app.data.FileCategory.VIDEOS
            if (isMedia) {
                coil.compose.AsyncImage(
                    model = file.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = file.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { onRemove(file.id) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DeviceCard(device: DiscoveredDevice, isSending: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceScale(interactionSource),
        onClick = onClick,
        enabled = !isSending,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSending) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainer
        ),
        interactionSource = interactionSource
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.alias,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSending) "Sending files..." else "${device.ip}:${device.port}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isSending) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

private suspend fun sendFilesToDevice(
    context: Context,
    device: DiscoveredDevice,
    files: List<SharedFile>,
    senderName: String
): String = withContext(Dispatchers.IO) {
    try {
        val sessionId = "session_${System.currentTimeMillis()}_to_${device.ip.replace(".", "")}"
        val db = AppDatabase.getDatabase(context)
        val sessionDao = db.transferSessionDao()

        var totalSize = 0L
        val fileEntities = files.mapIndexed { index, file ->
            totalSize += file.size
            TransferFileEntity(
                sessionId = sessionId,
                fileId = "file_$index",
                fileName = file.name,
                size = file.size,
                fileType = file.mimeType,
                uriString = file.uri.toString()
            )
        }

        val sessionEntity = TransferSessionEntity(
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

        val baseUrl = "http://${device.ip}:${device.port}"

        // Step 1: Prepare upload
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

        val prepareUrl = URL("$baseUrl/api/prepare-upload")
        val prepareConn = prepareUrl.openConnection() as HttpURLConnection
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
            return@withContext "Failed: No session ID from receiver"
        }

        // Step 2: Poll until receiver accepts (max 30 seconds)
        var remoteStatus = "pending"
        var pollCount = 0
        while (remoteStatus == "pending" && pollCount < 30) {
            delay(1000)
            pollCount++
            try {
                val statusUrl = URL("$baseUrl/api/session/status?sessionId=$remoteSessionId")
                val statusConn = statusUrl.openConnection() as HttpURLConnection
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

        // Step 3: Upload files
        var totalTransferred = 0L
        for (file in files) {
            val uri = Uri.parse(file.uri.toString())
            val uploadUrl = URL("$baseUrl/api/upload?sessionId=$remoteSessionId&fileId=${fileEntities[files.indexOf(file)].fileId}&filename=${file.name}")
            val uploadConn = uploadUrl.openConnection() as HttpURLConnection
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

        // Done
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
