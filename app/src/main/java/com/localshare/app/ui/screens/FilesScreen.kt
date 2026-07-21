package com.localshare.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.*
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.localshare.app.data.dataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.localshare.app.data.FileCategory
import com.localshare.app.data.SharedFile
import com.localshare.app.ui.FileShareViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesScreen(
    viewModel: FileShareViewModel,
    navController: NavController? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasSeenSwipeTutorialFlow = remember(context) {
        context.dataStore.data.map { it[booleanPreferencesKey("has_seen_swipe_tutorial")] ?: false }
    }
    val hasSeenSwipeTutorial by hasSeenSwipeTutorialFlow.collectAsState(initial = true)
    
    val shareConfig by viewModel.shareConfig.collectAsState()
    val sharedFiles = shareConfig.sharedFiles
    val selectedIds by viewModel.selectedFileIds.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    var sortBy by remember { mutableStateOf("name") }
    val sortedFiles = remember(sharedFiles, sortBy) {
        when (sortBy) {
            "size" -> sharedFiles.sortedByDescending { it.size }
            "date" -> sharedFiles.sortedByDescending { it.lastModified }
            "type" -> sharedFiles.sortedBy { it.category.name }
            else -> sharedFiles.sortedBy { it.name.lowercase() }
        }
    }
    val totalSize = sharedFiles.sumOf { it.size }
    val totalSizeStr = when {
        totalSize >= 1_073_741_824 -> String.format("%.1f GB", totalSize / 1_073_741_824.0)
        totalSize >= 1_048_576 -> String.format("%.1f MB", totalSize / 1_048_576.0)
        totalSize >= 1024 -> String.format("%.1f KB", totalSize / 1024.0)
        else -> "$totalSize B"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Selection toolbar (when in multi-select mode)
        AnimatedVisibility(
            visible = isMultiSelectMode,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SelectionToolbar(
                selectedCount = selectedIds.size,
                totalCount = sharedFiles.size,
                onSelectAll = { viewModel.selectAllFiles() },
                onDeselectAll = { viewModel.clearSelection() },
                onDeleteSelected = { viewModel.removeSelectedFiles() },
                onCancel = { viewModel.clearSelection() }
            )
        }

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
                    text = if (isMultiSelectMode) "${selectedIds.size} selected" else "Shared Files",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sharedFiles.size} item${if (sharedFiles.size != 1) "s" else ""} · $totalSizeStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (sharedFiles.isNotEmpty() && !isMultiSelectMode) {

                    OutlinedButton(
                        onClick = { viewModel.clearSharedFiles() },
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                    count = sortedFiles.size,
                    key = { sortedFiles[it].id }
                ) { index ->
                    val file = sortedFiles[index]
                    val isSelected = file.id in selectedIds
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    @OptIn(ExperimentalMaterial3Api::class)
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            when (dismissValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = file.mimeType
                                        putExtra(android.content.Intent.EXTRA_STREAM, file.uri)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share file"))
                                    } catch (e: Exception) {}
                                    false
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    viewModel.removeSharedFile(file.id)
                                    true
                                }
                                SwipeToDismissBoxValue.Settled -> false
                            }
                        }
                    )
                    
                    val scope = rememberCoroutineScope()
                    
                    Box {
                        @OptIn(ExperimentalMaterial3Api::class)
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = !isMultiSelectMode,
                            enableDismissFromEndToStart = !isMultiSelectMode,
                            backgroundContent = {
                                SwipeBackground(dismissState)
                            }
                        ) {
                            FileQueueItem(
                                file = file,
                                isSelected = isSelected,
                                isMultiSelectMode = isMultiSelectMode,
                                onRemove = { viewModel.removeSharedFile(file.id) },
                                onClick = {
                                    if (isMultiSelectMode) {
                                        viewModel.toggleFileSelection(file.id)
                                    } else {
                                        navController?.navigate("file_preview/${file.id}")
                                    }
                                },
                                onLongClick = {
                                    if (!isMultiSelectMode) {
                                        viewModel.toggleFileSelection(file.id)
                                    }
                                }
                            )
                        }
                        
                        if (index == 0 && !hasSeenSwipeTutorial && !isMultiSelectMode) {
                            SwipeTutorialOverlay(
                                modifier = Modifier.matchParentSize(),
                                onDismiss = {
                                    scope.launch {
                                        context.dataStore.edit { it[booleanPreferencesKey("has_seen_swipe_tutorial")] = true }
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun SelectionToolbar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount of $totalCount selected",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (selectedCount < totalCount) {
                    TextButton(onClick = onSelectAll) {
                        Text("All", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (selectedCount > 0) {
                    TextButton(onClick = onDeselectAll) {
                        Text("None", style = MaterialTheme.typography.labelMedium)
                    }
                }
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete selected",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel selection",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileQueueItem(
    file: SharedFile,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val borderColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
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
                            text = "\u2022",
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

            // Selection checkbox or Remove button
            if (isMultiSelectMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            } else {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection ?: return
    val color by androidx.compose.animation.animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.Settled -> Color.Transparent
            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
        },
        label = "SwipeColor"
    )
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    val icon = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Icons.Rounded.Share
        SwipeToDismissBoxValue.EndToStart -> Icons.Rounded.Delete
        SwipeToDismissBoxValue.Settled -> Icons.Rounded.Circle
    }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
        label = "SwipeIconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.scale(scale).size(28.dp)
        )
    }
}

@Composable
fun SwipeTutorialOverlay(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    
    if (!visible) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "swipe")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swipeOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f))
            .clickable { 
                visible = false
                onDismiss() 
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .offset(x = offsetX.dp)
                    .size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Swipe to Share or Delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap to dismiss",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
