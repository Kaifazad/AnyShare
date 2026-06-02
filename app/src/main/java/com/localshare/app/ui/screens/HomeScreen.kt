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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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

@Composable
fun HomeScreen(viewModel: FileShareViewModel) {
    val isRunning by viewModel.isServerRunning.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val connectedDevices by viewModel.connectedDeviceCount.collectAsState()
    val shareConfig by viewModel.shareConfig.collectAsState()
    val settings by viewModel.appSettings.collectAsState()
    var showReceiveSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.addCustomFolder(it.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ─── Status Header ──────────────────────────────────────
            StatusIndicator(isRunning = isRunning)

            // ─── Start/Stop FAB ─────────────────────────────────────
            ServerControlButton(
                isRunning = isRunning,
                onToggle = { viewModel.toggleServer() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // ─── Receive Files Button ───────────────────────────────
        androidx.compose.material3.OutlinedButton(
            onClick = { showReceiveSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
        ) {
            Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Receive")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Receive from Device", fontWeight = FontWeight.Bold)
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
        AnimatedVisibility(
            visible = isRunning && serverUrl != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            serverUrl?.let { url ->
                ServerUrlCard(
                    url = url,
                    deviceName = settings.deviceName,
                    connectedDevices = connectedDevices,
                    onCopy = { copyToClipboard(context, url) },
                    onEditName = { showNameDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ─── Category Toggles ───────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Shared Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Toggle categories to include in sharing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FileCategory.entries.forEach { category ->
                    if (category != FileCategory.CUSTOM_FOLDERS) {
                        CategoryToggleRow(
                            category = category,
                            isEnabled = shareConfig.isCategoryEnabled(category),
                            onToggle = { viewModel.toggleCategory(category) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // ─── Custom Folders Section ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Folders",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { folderPickerLauncher.launch(null) }) {
                        Text("Add Folder")
                    }
                }

                if (shareConfig.customFolderUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    shareConfig.customFolderUris.forEach { uriString ->
                        val folderName = try {
                            val uri = Uri.parse(uriString)
                            val docFile = DocumentFile.fromTreeUri(context, uri)
                            docFile?.name ?: "Unknown Folder"
                        } catch (_: Exception) {
                            "Unknown Folder"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = folderName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.removeCustomFolder(uriString) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Remove folder",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showReceiveSheet) {
        ReceiveBottomSheet(onDismiss = { showReceiveSheet = false })
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

    Row(
        modifier = Modifier
            .height(48.dp)
            .width(160.dp)
            .clip(RoundedCornerShape(50))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer { alpha = if (isRunning) pulseAlpha else 1f }
                .background(statusColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isRunning) "Connected" else "Disconnected",
            style = MaterialTheme.typography.labelLarge,
            color = statusColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ServerControlButton(isRunning: Boolean, onToggle: () -> Unit) {
    val stopRed = Color(0xFFEF4444)

    val buttonColor by animateColorAsState(
        targetValue = if (isRunning) stopRed else MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = "buttonColor"
    )

    androidx.compose.material3.Button(
        onClick = onToggle,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        ),
        modifier = Modifier.height(48.dp).width(160.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Device Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onEditName, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Name",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // URL + Copy
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
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

// ─── Category Toggle Row ───────────────────────────────────────────

@Composable
private fun CategoryToggleRow(
    category: FileCategory,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (category) {
            FileCategory.VIDEOS -> Icons.Rounded.Movie
            FileCategory.PHOTOS -> Icons.Rounded.Image
            FileCategory.AUDIO -> Icons.Rounded.AudioFile
            FileCategory.DOCUMENTS -> Icons.Rounded.Description
            FileCategory.DOWNLOADS -> Icons.Rounded.Download
            FileCategory.APPS -> Icons.Rounded.Android
            FileCategory.CUSTOM_FOLDERS -> Icons.Rounded.Folder
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = Color.White
            )
        )
    }
}

// ─── Helpers ───────────────────────────────────────────────────────

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("LocalShare URL", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
}
