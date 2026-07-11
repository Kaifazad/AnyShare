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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferHistoryScreen(
    onNavigateBack: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.transferSessionDao()
    
    val sessions by dao.getAllSessions().collectAsState(initial = emptyList())

    // No Scaffold/TopAppBar — MainFlow already provides the top bar
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No transfers yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Files downloaded via browser will not appear here.\nThis tab only shows direct phone-to-phone transfers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        val scope = rememberCoroutineScope()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions, key = { it.sessionId }) { session ->
                HistoryItem(
                    session = session,
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            dao.deleteSession(session.sessionId)
                        }
                    },
                    onClick = { navController?.navigate("history_details/${session.sessionId}") }
                )
            }
        }
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
