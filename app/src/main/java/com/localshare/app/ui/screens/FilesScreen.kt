package com.localshare.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.localshare.app.data.FileCategory
import com.localshare.app.data.SharedFile
import com.localshare.app.ui.FileShareViewModel

@Composable
fun FilesScreen(viewModel: FileShareViewModel) {
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Currently Sharing",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sharedFiles.size} items selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (sharedFiles.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearSharedFiles() }) {
                    Text("Clear All")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sharedFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nothing is shared yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Go to the Home tab and tap a button\nto select files to share.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = sharedFiles.size,
                    key = { sharedFiles[it].id }
                ) { index ->
                    val file = sharedFiles[index]
                    FileQueueItem(
                        file = file,
                        onRemove = { viewModel.removeSharedFile(file.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FileQueueItem(
    file: SharedFile,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(file.category).copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val isMedia = file.category == FileCategory.PHOTOS || file.category == FileCategory.VIDEOS
                if (isMedia) {
                    AsyncImage(
                        model = file.uri,
                        contentDescription = file.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIcon(file.category),
                        contentDescription = null,
                        tint = getCategoryColor(file.category),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (file.size > 0) {
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = file.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Remove Button
            val interactionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(36.dp)
                    .bounceScale(interactionSource),
                interactionSource = interactionSource
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── Helpers ───────────────────────────────────────────────────────

@Composable
private fun getCategoryColor(category: FileCategory) = when (category) {
    FileCategory.VIDEOS -> MaterialTheme.colorScheme.error
    FileCategory.PHOTOS -> MaterialTheme.colorScheme.secondary
    FileCategory.AUDIO -> MaterialTheme.colorScheme.tertiary
    FileCategory.DOCUMENTS -> MaterialTheme.colorScheme.primary
    FileCategory.DOWNLOADS -> MaterialTheme.colorScheme.primary
    FileCategory.APPS -> MaterialTheme.colorScheme.tertiary
    FileCategory.CUSTOM_FOLDERS -> MaterialTheme.colorScheme.primary
}

private fun getCategoryIcon(category: FileCategory): ImageVector = when (category) {
    FileCategory.VIDEOS -> Icons.Rounded.Movie
    FileCategory.PHOTOS -> Icons.Rounded.Image
    FileCategory.AUDIO -> Icons.Rounded.AudioFile
    FileCategory.DOCUMENTS -> Icons.Rounded.Description
    FileCategory.DOWNLOADS -> Icons.Rounded.Download
    FileCategory.APPS -> Icons.Rounded.Android
    FileCategory.CUSTOM_FOLDERS -> Icons.Rounded.Folder
}
