package com.localshare.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.localshare.app.ui.components.QrCodeImage
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import com.localshare.app.data.FileCategory
import com.localshare.app.data.SharedFile
import com.localshare.app.ui.FileShareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(viewModel: FileShareViewModel) {
    val allFiles by viewModel.allFiles.collectAsState()
    val shareConfig by viewModel.shareConfig.collectAsState()
    val isLoading by viewModel.isLoadingFiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isRunning by viewModel.isServerRunning.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    var showSendDialog by remember { mutableStateOf(false) }

    var sortMode by remember { mutableStateOf("name") }
    var isGridView by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedFolderName by remember { mutableStateOf<String?>(null) }

    val filteredFiles by remember(allFiles, searchQuery, selectedCategory, sortMode) {
        derivedStateOf {
            val files = viewModel.getFilteredFiles()
            when (sortMode) {
                "size" -> files.sortedByDescending { it.size }
                "date" -> files.sortedByDescending { it.lastModified }
                else -> files.sortedBy { it.name.lowercase() }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
        // ─── Material 3 Rounded Search Bar ──────────────────────
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(28.dp)),
            placeholder = {
                Text(
                    "Search files...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Category Chips (fully rounded) ─────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { 
                        viewModel.setSelectedCategory(null) 
                        selectedFolderName = null
                    },
                    label = { Text("All") },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Category chips
            items(FileCategory.entries.toList()) { category ->
                val count = allFiles[category]?.size ?: 0
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { 
                        viewModel.setSelectedCategory(category) 
                        selectedFolderName = null
                    },
                    label = {
                        Text("${category.displayName} ($count)")
                    },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ─── Controls Row (Compact & Professional) ──────────────
        val isAppCategory = selectedCategory == FileCategory.APPS
        val showFolderNavigation = !isAppCategory && selectedFolderName == null

        val filesToDisplay = if (selectedFolderName != null) {
            filteredFiles.filter { it.parentFolderName == selectedFolderName }
        } else {
            filteredFiles
        }

        if (selectedFolderName != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedFolderName = null }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Select All / Clear All (Pill Buttons)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.FilledTonalButton(
                        onClick = {
                            val category = selectedCategory
                            if (category != null) {
                                viewModel.selectAllInCategory(category)
                            } else {
                                FileCategory.entries.forEach { viewModel.selectAllInCategory(it) }
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Filled.SelectAll,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select All", style = MaterialTheme.typography.labelMedium)
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            val category = selectedCategory
                            if (category != null) {
                                viewModel.deselectAllInCategory(category)
                            } else {
                                FileCategory.entries.forEach { viewModel.deselectAllInCategory(it) }
                            }
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Right side: Sort + View toggle
                if (!showFolderNavigation) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))) {
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Name",
                                                fontWeight = if (sortMode == "name") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = { sortMode = "name"; showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Size",
                                                fontWeight = if (sortMode == "size") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = { sortMode = "size"; showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Date",
                                                fontWeight = if (sortMode == "date") FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = { sortMode = "date"; showSortMenu = false }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = "Toggle View",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ─── File List / Grid ───────────────────────────────────
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            filteredFiles.isEmpty() -> {
                EmptyFilesState(hasSearch = searchQuery.isNotBlank())
            }

            showFolderNavigation && filesToDisplay.isNotEmpty() -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val groupedFiles = filesToDisplay.groupBy { it.parentFolderName }
                    items(
                        items = groupedFiles.entries.toList(),
                        key = { it.key }
                    ) { (folderName, filesInFolder) ->
                        FolderListItem(
                            folderName = folderName,
                            fileCount = filesInFolder.size,
                            isSelected = filesInFolder.all { shareConfig.isFileSelected(it.id) || shareConfig.isCategoryEnabled(it.category) },
                            onToggleSelect = {
                                val allSelected = filesInFolder.all { shareConfig.isFileSelected(it.id) || shareConfig.isCategoryEnabled(it.category) }
                                if (allSelected) {
                                    viewModel.deselectFiles(filesInFolder.map { it.id }.toSet())
                                } else {
                                    viewModel.selectFiles(filesInFolder.map { it.id }.toSet())
                                }
                            },
                            onClick = { selectedFolderName = folderName }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            isGridView -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filesToDisplay,
                        key = { it.id }
                    ) { file ->
                        FileGridItem(
                            file = file,
                            isSelected = shareConfig.isFileSelected(file.id) ||
                                    shareConfig.isCategoryEnabled(file.category),
                            onToggle = { viewModel.toggleFile(file.id) },
                            isCategoryEnabled = shareConfig.isCategoryEnabled(file.category)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filesToDisplay,
                        key = { it.id }
                    ) { file ->
                        FileItem(
                            file = file,
                            isSelected = shareConfig.isFileSelected(file.id) ||
                                    shareConfig.isCategoryEnabled(file.category),
                            onToggle = { viewModel.toggleFile(file.id) },
                            isCategoryEnabled = shareConfig.isCategoryEnabled(file.category)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // ─── Floating Action Button ────────────────────────────────
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedVisibility(
                visible = shareConfig.selectedFileIds.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
            ExtendedFloatingActionButton(
                onClick = { 
                    if (!isRunning) {
                        viewModel.toggleServer()
                    }
                    showSendDialog = true 
                },
                icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null) },
                text = { Text("Send Selected") }
            )
            }
        }
    }
    
    // ─── QR Code Dialog ─────────────────────────────────────────
    if (showSendDialog) {
        AlertDialog(
            onDismissRequest = { showSendDialog = false },
            title = { Text("Scan to Receive") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (serverUrl != null) {
                        QrCodeImage(
                            url = serverUrl!!,
                            foregroundColor = android.graphics.Color.BLACK,
                            backgroundColor = android.graphics.Color.WHITE
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = serverUrl!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Starting server...")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSendDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
} // Closes FilesScreen

// ─── File Item (List View) ─────────────────────────────────────────

@Composable
private fun FileItem(
    file: SharedFile,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isCategoryEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isCategoryEnabled) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or icon
            FileThumbnail(file = file, size = 48)

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
                    Text(
                        text = file.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Checkbox
            Icon(
                imageVector = if (isSelected)
                    Icons.Filled.CheckBox
                else
                    Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─── File Item (Grid / Card View) ──────────────────────────────────

@Composable
private fun FileGridItem(
    file: SharedFile,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isCategoryEnabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isCategoryEnabled) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(getCategoryColor(file.category).copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                val isMedia = file.category == FileCategory.PHOTOS || file.category == FileCategory.VIDEOS
                if (isMedia) {
                    AsyncImage(
                        model = file.uri,
                        contentDescription = file.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = getCategoryIcon(file.category),
                        contentDescription = null,
                        tint = getCategoryColor(file.category),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Selection badge
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckBox,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = file.formattedSize,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Thumbnail Composable ──────────────────────────────────────────

@Composable
private fun FileThumbnail(file: SharedFile, size: Int) {
    val isMedia = file.category == FileCategory.PHOTOS || file.category == FileCategory.VIDEOS

    if (isMedia) {
        AsyncImage(
            model = file.uri,
            contentDescription = file.name,
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(getCategoryColor(file.category).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(file.category),
                contentDescription = null,
                tint = getCategoryColor(file.category),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ─── Empty State ───────────────────────────────────────────────────

@Composable
private fun EmptyFilesState(hasSearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearch) "No files match your search" else "No files found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearch) "Try a different search term"
                else "Grant storage permission to see files",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
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
    FileCategory.VIDEOS -> Icons.Filled.VideoFile
    FileCategory.PHOTOS -> Icons.Filled.Image
    FileCategory.AUDIO -> Icons.Filled.AudioFile
    FileCategory.DOCUMENTS -> Icons.Filled.Description
    FileCategory.DOWNLOADS -> Icons.Filled.Download
    FileCategory.APPS -> Icons.Filled.Android
    FileCategory.CUSTOM_FOLDERS -> Icons.Filled.Folder
}

// ─── Folder Header ───────────────────────────────────────────────

@Composable
private fun FolderHeader(
    folderName: String,
    fileCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folderName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$fileCount items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.TextButton(
                onClick = onSelectAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Select All", style = MaterialTheme.typography.labelMedium)
            }
            androidx.compose.material3.TextButton(
                onClick = onDeselectAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Clear", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

// ─── Folder List Item (For Navigation) ───────────────────────────

@Composable
private fun FolderListItem(
    folderName: String,
    fileCount: Int,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Folder info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folderName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$fileCount items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Checkbox
            IconButton(onClick = onToggleSelect) {
                Icon(
                    imageVector = if (isSelected)
                        Icons.Filled.CheckBox
                    else
                        Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
