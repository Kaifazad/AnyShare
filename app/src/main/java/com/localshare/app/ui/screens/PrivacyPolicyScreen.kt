package com.localshare.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Last updated: July 10, 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PrivacySection(
                title = "1. Overview",
                content = "LocalShare is an offline file sharing application designed to transfer files between devices on the same local network. We are committed to protecting your privacy."
            )

            PrivacySection(
                title = "2. Data Collection",
                content = "LocalShare is designed with privacy as a core principle:\n\n" +
                    "\u2022 No data is sent to external servers. All file transfers happen directly between devices on your local network.\n" +
                    "\u2022 No analytics or tracking. The app does not collect usage statistics or personal data.\n" +
                    "\u2022 No account required. No registration, login, or account creation.\n" +
                    "\u2022 No internet required. The app works entirely offline."
            )

            PrivacySection(
                title = "3. File Access",
                content = "To share files, LocalShare requires access to your device storage. This access is used solely to:\n\n" +
                    "\u2022 Browse and select files you explicitly choose to share.\n" +
                    "\u2022 Generate thumbnails for file preview in the web UI.\n\n" +
                    "Files are only accessible when you explicitly share them."
            )

            PrivacySection(
                title = "4. Network Communication",
                content = "LocalShare creates a local HTTP server on your device:\n\n" +
                    "\u2022 Only listens on your local network (Wi-Fi or hotspot).\n" +
                    "\u2022 Does not communicate with any external servers.\n" +
                    "\u2022 Can be protected with an optional PIN code.\n" +
                    "\u2022 Can optionally encrypt transfers using AES-256-GCM."
            )

            PrivacySection(
                title = "5. Notifications",
                content = "LocalShare may request notification permission to inform you about:\n\n" +
                    "\u2022 Server status (running/stopped).\n" +
                    "\u2022 File access events."
            )

            PrivacySection(
                title = "6. Data Storage",
                content = "All app settings are stored locally on your device using Android DataStore. Shared file lists and transfer history are stored in a local Room database. None of this data leaves your device."
            )

            PrivacySection(
                title = "7. Contact",
                content = "If you have questions about this Privacy Policy, please open an issue on our GitHub repository."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
