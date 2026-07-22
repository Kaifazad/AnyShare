package com.localshare.app.ui.screens

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.localshare.app.data.db.AppDatabase
import com.localshare.app.data.db.TransferFileEntity
import java.io.File
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferHistoryDetailsScreen(
    sessionId: String,
    onBack: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.transferSessionDao()

    var files by remember { mutableStateOf<List<TransferFileEntity>>(emptyList()) }

    LaunchedEffect(sessionId) {
        files = dao.getFilesForSession(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Files in this session:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(files, key = { it.id }) { fileInfo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AnyShare")
                            val localFile = File(dir, fileInfo.fileName)

                            if (localFile.exists()) {
                                val uri = Uri.fromFile(localFile).toString()
                                val encodedUri = URLEncoder.encode(uri, "UTF-8")
                                val encodedName = URLEncoder.encode(fileInfo.fileName, "UTF-8")
                                val encodedType = URLEncoder.encode(fileInfo.fileType, "UTF-8")
                                navController.navigate("uri_preview?uri=$encodedUri&name=$encodedName&type=$encodedType")
                            } else {
                                Toast.makeText(context, "File not found locally.", Toast.LENGTH_SHORT).show()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when {
                            fileInfo.fileType.startsWith("image/") -> Icons.Rounded.Image
                            fileInfo.fileType.startsWith("video/") -> Icons.Rounded.Movie
                            fileInfo.fileType.startsWith("audio/") -> Icons.Rounded.AudioFile
                            else -> Icons.AutoMirrored.Rounded.InsertDriveFile
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileInfo.fileName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatSize(fileInfo.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
    bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
    else -> "$bytes B"
}
