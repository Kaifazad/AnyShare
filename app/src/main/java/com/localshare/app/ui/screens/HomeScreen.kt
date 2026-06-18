package com.localshare.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import com.localshare.app.service.ServerForegroundService
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Folder
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.localshare.app.data.FileCategory
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.utils.bounceClick
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun HomeScreen(viewModel: FileShareViewModel) {
    val isRunning by viewModel.isServerRunning.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val connectedDevices by viewModel.connectedDeviceCount.collectAsState()
    val shareConfig by viewModel.shareConfig.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    var showReceiveSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val fileRepository = remember { com.localshare.app.data.FileRepository(context) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(OpenFilesAtRootContract()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
            }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val files = fileRepository.resolveUris(uris)
                viewModel.addSharedFiles(files)
            }
        }
    }

    var showTextPasteDialog by remember { mutableStateOf(false) }
    var showAppPickerSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Status Card ────────────────────────────────────────
        StatusIndicator(isRunning = isRunning)

        Spacer(modifier = Modifier.height(20.dp))

        // ─── Split-Shape Action Buttons ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Start/Stop Server — rounded left
            ServerControlButton(
                isRunning = isRunning,
                onToggle = { viewModel.toggleServer() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp)
            )

            // Receive from Device — rounded right
            val receiveInteractionSource = remember { MutableInteractionSource() }
            androidx.compose.material3.FilledTonalButton(
                onClick = { showReceiveSheet = true },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .bounceScale(receiveInteractionSource),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 28.dp, bottomEnd = 28.dp),
                interactionSource = receiveInteractionSource
            ) {
                Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Receive", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Receive", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        var showNameDialog: Boolean by remember { mutableStateOf(false) }

        if (showNameDialog) {
            var tempName: String by remember { mutableStateOf(settings.deviceName) }
            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Device Name") },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.setDeviceName(tempName.trim())
                        }
                        showNameDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
                }
            )
        }

        // ─── URL Card with QR ───────────────────────────────────
        var displayUrl by remember { mutableStateOf("") }
        if (serverUrl != null) {
            displayUrl = serverUrl!!
        }

        AnimatedVisibility(
            visible = isRunning && serverUrl != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ServerUrlCard(
                url = displayUrl,
                deviceName = settings.deviceName,
                connectedDevices = connectedDevices,
                onCopy = { copyToClipboard(context, displayUrl) },
                onEditName = { showNameDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Share Grid ───────────────────────────────────
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = "Share Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                )

        Spacer(modifier = Modifier.height(16.dp))

        // Single row: Media, Files, Apps, Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareGridButton(
                icon = Icons.Rounded.Image,
                label = "Media",
                onClick = {
                    mediaPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            )
            ShareGridButton(
                icon = Icons.Rounded.Description,
                label = "Files",
                onClick = {
                    filePicker.launch("*/*")
                }
            )
            ShareGridButton(
                icon = Icons.Rounded.Android,
                label = "Apps",
                onClick = {
                    showAppPickerSheet = true
                }
            )
            ShareGridButton(
                icon = Icons.Filled.ContentCopy,
                label = "Text",
                onClick = {
                    showTextPasteDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Selected files will appear in the Files tab",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    } // End Column inside ElevatedCard
} // End ElevatedCard

Spacer(modifier = Modifier.height(120.dp))
} // End Main Column

    if (showReceiveSheet) {
        ReceiveBottomSheet(onDismiss = { showReceiveSheet = false })
    }

    if (showTextPasteDialog) {
        var pastedText by remember { mutableStateOf("") }
        var createTextFile by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showTextPasteDialog = false },
            title = { Text("Share Text") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        label = { Text("Paste text or links here") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { createTextFile = !createTextFile },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = createTextFile,
                            onCheckedChange = { createTextFile = it }
                        )
                        Text("Create a downloadable text file", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pastedText.isNotBlank()) {
                        if (createTextFile) {
                            val textId = pastedText.hashCode().toLong()
                            val textFile = com.localshare.app.data.SharedFile(
                                id = textId,
                                name = "SharedText_${System.currentTimeMillis()}.txt",
                                path = "virtual://$pastedText",
                                uri = Uri.parse("virtual://$textId"),
                                size = pastedText.toByteArray().size.toLong(),
                                mimeType = "text/plain",
                                category = FileCategory.DOCUMENTS,
                                lastModified = System.currentTimeMillis()
                            )
                            viewModel.addSharedFiles(listOf(textFile))
                        }

                        // Update the internal server clipboard for web UI syncing without overwriting the OS clipboard
                        try {
                            ServerForegroundService.updateServerClipboard(pastedText)
                            val toastMsg = if (createTextFile) "Shared text file to web UI" else "Text shared to web UI"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    showTextPasteDialog = false
                }) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextPasteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAppPickerSheet) {
        // App Picker implementation will go here
        AppPickerBottomSheet(
            onDismiss = { showAppPickerSheet = false },
            onAppSelected = { appFile ->
                viewModel.addSharedFiles(listOf(appFile))
                showAppPickerSheet = false
            }
        )
    }
}

@Composable
fun ShareGridButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val iconColor = MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .bounceClick(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp)
            .width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

// ─── Status Indicator ──────────────────────────────────────────────

@Composable
private fun StatusIndicator(isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)

    val statusColor by animateColorAsState(
        targetValue = if (isRunning) greenColor else redColor,
        animationSpec = tween(500),
        label = "statusColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .graphicsLayer { alpha = if (isRunning) pulseAlpha else 1f }
                    .background(statusColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isRunning) "Server Running" else "Server Offline",
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ServerControlButton(
    isRunning: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(50)
) {
    val stopRed = Color(0xFFEF4444)

    val buttonColor by animateColorAsState(
        targetValue = if (isRunning) stopRed else MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = "buttonColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    androidx.compose.material3.Button(
        onClick = onToggle,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        ),
        modifier = modifier.bounceScale(interactionSource),
        shape = shape,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = if (isRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isRunning) "Stop Server" else "Start Server",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isRunning) "Stop Server" else "Start Server",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Server URL Card ───────────────────────────────────────────────

@Composable
private fun ServerUrlCard(
    url: String,
    deviceName: String,
    connectedDevices: Int,
    onCopy: () -> Unit,
    onEditName: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Device Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Device Name - $deviceName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditName, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Name",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val port = url.substringAfterLast(":")
            val magicLinkUrl = "http://localshare.local:$port"

            var showQrCode by remember { mutableStateOf(false) }

            // Expandable QR Code Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .bounceClick { showQrCode = !showQrCode },
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showQrCode) "Hide QR Code" else "Show QR Code",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (showQrCode) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = "Toggle QR",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showQrCode) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(12.dp)
                            ) {
                                com.localshare.app.ui.components.QrCodeImage(
                                    url = magicLinkUrl,
                                    size = 140.dp,
                                    foregroundColor = android.graphics.Color.BLACK,
                                    backgroundColor = android.graphics.Color.WHITE
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // URL 1 (IP Address) + Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // URL 2 (Magic Link) + Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = magicLinkUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { copyToClipboard(context, magicLinkUrl) }, 
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Magic Link",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connected devices
            if (connectedDevices > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Devices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$connectedDevices devices connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── App Picker Bottom Sheet ──────────────────────────────────────

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppPickerBottomSheet(onDismiss: () -> Unit, onAppSelected: (com.localshare.app.data.SharedFile) -> Unit) {
    val context = LocalContext.current
    val fileRepository = remember { com.localshare.app.data.FileRepository(context) }
    var apps by remember { mutableStateOf<List<com.localshare.app.data.SharedFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            apps = fileRepository.getInstalledApps()
            isLoading = false
        }
    }

    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select App to Share",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(apps.size) { index ->
                        val app = apps[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppSelected(app) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Android,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = app.name.removeSuffix(".apk"),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(48.dp)) }
                }
            }
        }
    }
}

// ─── Helpers ───────────────────────────────────────────────────────

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("LocalShare URL", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
}

class OpenFilesAtRootContract : androidx.activity.result.contract.ActivityResultContract<String, List<android.net.Uri>>() {
    override fun createIntent(context: android.content.Context, input: String): android.content.Intent {
        return android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(android.content.Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(
                android.provider.DocumentsContract.EXTRA_INITIAL_URI,
                android.net.Uri.parse("content://com.android.externalstorage.documents/document/primary%3A")
            )
        }
    }

    override fun parseResult(resultCode: Int, intent: android.content.Intent?): List<android.net.Uri> {
        if (resultCode != android.app.Activity.RESULT_OK || intent == null) return emptyList()
        val uris = mutableListOf<android.net.Uri>()
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                uris.add(clipData.getItemAt(i).uri)
            }
        } ?: intent.data?.let { uris.add(it) }
        return uris
    }
}
