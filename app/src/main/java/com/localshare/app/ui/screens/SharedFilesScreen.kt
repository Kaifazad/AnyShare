package com.localshare.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.localshare.app.data.SessionStatus
import com.localshare.app.data.TransferType
import com.localshare.app.data.db.AppDatabase
import com.localshare.app.data.db.TransferSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.data.SharedFile
import com.localshare.app.data.FileCategory
import coil.compose.AsyncImage
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SharedFilesScreen(
    viewModel: FileShareViewModel,
    onNavigateBack: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles
    val scope = rememberCoroutineScope()
    var selectedFileForPreview by remember { mutableStateOf<SharedFile?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Current Sharing Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shared Files (${sharedFiles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (sharedFiles.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearSharedFiles() }) {
                        Text("Clear All")
                    }
                }
            }
        }
        
        if (sharedFiles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No files selected for sharing.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sharedFiles.forEach { file ->
                        FileChip(
                            file = file,
                            onRemove = { viewModel.removeSharedFile(it) },
                            onClick = { id -> selectedFileForPreview = sharedFiles.find { it.id == id } }
                        )
                    }
                }
            }
        }
        

        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Nav bar padding
        }
    }

    if (selectedFileForPreview != null) {
        FilePreviewBottomSheet(
            file = selectedFileForPreview!!,
            onDismiss = { selectedFileForPreview = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePreviewBottomSheet(file: SharedFile, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Media Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (file.category == FileCategory.PHOTOS) {
                    AsyncImage(
                        model = file.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (file.category == FileCategory.VIDEOS) {
                    VideoPlayer(uri = file.uri)
                } else if (file.mimeType == "application/pdf" || file.name.endsWith(".pdf", ignoreCase = true)) {
                    PdfPreview(uri = file.uri)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = file.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = file.formattedSize, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (file.category != FileCategory.PHOTOS && file.category != FileCategory.VIDEOS) {
                Button(onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(file.uri, file.mimeType)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Ignore or handle
                    }
                }) {
                    Text("Open Externally")
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun VideoPlayer(uri: android.net.Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = { ctx ->
            val view = android.view.LayoutInflater.from(ctx).inflate(com.localshare.app.R.layout.view_video_player, null) as PlayerView
            view.apply {
                player = exoPlayer
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PdfPreview(uri: android.net.Uri) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    androidx.compose.runtime.LaunchedEffect(uri) {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                    val renderer = android.graphics.pdf.PdfRenderer(fd)
                    if (renderer.pageCount > 0) {
                        val page = renderer.openPage(0)
                        val bmp = android.graphics.Bitmap.createBitmap(
                            page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(bmp)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        
                        page.render(bmp, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        renderer.close()
                        bitmap = bmp
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryItem(session: TransferSessionEntity, onDelete: () -> Unit, onClick: () -> Unit = {}) {
    val isSend = session.transferType == TransferType.SEND
    val icon = if (isSend) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
    val color = if (isSend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    // Build a meaningful title
    val title = when {
        isSend && session.senderName.isNotBlank() -> "To ${session.senderName}"
        isSend -> "To ${session.senderIp}"
        session.senderName.isNotBlank() -> "From ${session.senderName}"
        else -> "From ${session.senderIp}"
    }

    // Format timestamp
    val timeStr = remember(session.startTime) {
        val sdf = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(session.startTime))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${session.totalFiles} files • ${formatSize(session.totalSize)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (session.status == SessionStatus.ACTIVE && session.totalSize > 0) {
                    LinearProgressIndicator(
                        progress = { session.transferredBytes.toFloat() / session.totalSize.toFloat() },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            StatusBadge(session.status)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: SessionStatus) {
    val (color, text) = when (status) {
        SessionStatus.COMPLETED -> MaterialTheme.colorScheme.primary to "Done"
        SessionStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary to "Active"
        SessionStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
        SessionStatus.REJECTED -> MaterialTheme.colorScheme.error to "Rejected"
        SessionStatus.PENDING -> MaterialTheme.colorScheme.outline to "Pending"
        SessionStatus.CANCELLED -> MaterialTheme.colorScheme.outline to "Cancelled"
        SessionStatus.PAUSED -> MaterialTheme.colorScheme.outline to "Paused"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
    bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
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
            val isVideo = file.category == com.localshare.app.data.FileCategory.VIDEOS
            if (isMedia) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = file.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Play button overlay for videos
                    if (isVideo) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
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
