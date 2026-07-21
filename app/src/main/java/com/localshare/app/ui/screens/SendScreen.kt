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
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.localshare.app.data.SharedFile
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.utils.bounceScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SendScreen(
    viewModel: FileShareViewModel,
    navController: NavController,
    onFilesSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles
    val scope = rememberCoroutineScope()
    val fileRepository = remember { com.localshare.app.data.FileRepository(context) }
    val isRunning by viewModel.isServerRunning.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    var showTextPasteDialog by remember { mutableStateOf(false) }
    var showAppPickerSheet by remember { mutableStateOf(false) }


    val filePicker = rememberLauncherForActivityResult(OpenFilesAtRootContract()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onFilesSelected()
                }
            }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onFilesSelected()
                }
            }
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                val folder = fileRepository.resolveFolder(uri)
                if (folder != null) {
                    viewModel.addSharedFiles(listOf(folder))
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        onFilesSelected()
                    }
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
                text = "Share Items",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select files, apps, or text to host on your local server.",
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

    }
    // Spacer for bottom navigation
    Spacer(modifier = Modifier.height(100.dp))
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
                        onFilesSelected()
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
                    onFilesSelected()
                }
                showAppPickerSheet = false
            }
        )
    }
}

