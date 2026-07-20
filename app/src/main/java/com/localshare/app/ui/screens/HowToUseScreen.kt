package com.localshare.app.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("How to Use", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader(title = "Getting Started")

            HowToCard(
                stepNumber = 1,
                icon = Icons.Rounded.WifiTethering,
                iconColor = Color(0xFF4285F4),
                title = "Connect to the Same Network",
                description = "Both devices must be on the same network. You have two options:"
            ) {
                SubStep(
                    label = "Wi-Fi",
                    detail = "Connect both devices to the same Wi-Fi router. This is the easiest and fastest method."
                )
                SubStep(
                    label = "Hotspot",
                    detail = "Turn on your phone's hotspot and connect the other device to it. Go to Settings > Hotspot to enable it."
                )
                TipCard(text = "Make sure Wi-Fi is turned ON on this device, even when using hotspot mode. The server needs an active network interface.")
            }

            HowToCard(
                stepNumber = 2,
                icon = Icons.Rounded.Share,
                iconColor = Color(0xFF2E7D32),
                title = "Select Files to Share",
                description = "Choose what you want to share from the Send tab:"
            ) {
                SubStep(
                    label = "Media",
                    detail = "Share photos and videos directly from your gallery."
                )
                SubStep(
                    label = "Files",
                    detail = "Browse and select any file type - documents, APKs, archives, etc."
                )
                SubStep(
                    label = "Folders",
                    detail = "Share an entire folder. It will be automatically zipped for download."
                )
                SubStep(
                    label = "Apps",
                    detail = "Share installed apps as APK files with nearby devices."
                )
                SubStep(
                    label = "Text",
                    detail = "Paste or type text to share it instantly via the clipboard sync feature."
                )
            }

            HowToCard(
                stepNumber = 3,
                icon = Icons.Rounded.Bolt,
                iconColor = Color(0xFFFF8F00),
                title = "Start the Server",
                description = "Tap the large \"Start Server\" button on the Home tab. You will see:"
            ) {
                SubStep(
                    label = "Server URL",
                    detail = "Something like http://192.168.1.5:8080 - this is what the other device needs to open."
                )
                SubStep(
                    label = "Green Indicator",
                    detail = "A pulsing green dot means the server is running and ready."
                )
                SubStep(
                    label = "QR Code",
                    detail = "Tap \"Show QR Code\" to display a scannable code for quick connection."
                )
            }

            HowToCard(
                stepNumber = 4,
                icon = Icons.Rounded.Devices,
                iconColor = Color(0xFF7C3AED),
                title = "Connect from the Other Device",
                description = "On the receiving device, open a web browser (Chrome, Safari, Firefox, etc.) and enter the server URL."
            ) {
                SubStep(
                    label = "Type the URL",
                    detail = "Enter the full URL shown on your phone, e.g. http://192.168.1.5:8080"
                )
                SubStep(
                    label = "Browse and Download",
                    detail = "The web UI shows all shared files. Tap any file to preview or download it."
                )
                SubStep(
                    label = "Upload from Browser",
                    detail = "Other devices can also upload files TO your phone using the \"Upload to Phone\" button in the web UI."
                )
            }

            HowToCard(
                stepNumber = 5,
                icon = Icons.Rounded.CheckCircle,
                iconColor = Color(0xFF00897B),
                title = "App-to-App Transfer",
                description = "If the other device also has LocalShare installed, you can use the Nearby tab for direct phone-to-phone transfers:"
            ) {
                SubStep(
                    label = "Discover Devices",
                    detail = "Both devices auto-discover each other on the same network via mDNS."
                )
                SubStep(
                    label = "Send Push",
                    detail = "Select a device from the list and tap \"Send\" to push files directly."
                )
                SubStep(
                    label = "Accept or Reject",
                    detail = "The receiver gets a notification and dialog to accept or reject the transfer."
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SectionHeader(title = "Features")

            FeatureItem(
                icon = Icons.Rounded.ContentCopy,
                title = "Clipboard Sync",
                description = "Share clipboard text between your phone and laptop in real-time. Open the clipboard panel from the web UI to send and receive text."
            )

            FeatureItem(
                icon = Icons.Rounded.Lock,
                title = "PIN Protection",
                description = "Set a 4-digit PIN in Settings to protect your shared files. Other devices must enter the PIN before they can access anything."
            )

            FeatureItem(
                icon = Icons.Rounded.Security,
                title = "Encryption",
                description = "Enable encryption in Settings for AES-256-GCM encrypted file transfers. A unique session key is generated and delivered only to authenticated clients (not embedded in the share URL)."
            )

            FeatureItem(
                icon = Icons.Rounded.Folder,
                title = "Transfer History",
                description = "View a log of all file transfers in the History tab. See what was shared, when, and by which device."
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            SectionHeader(title = "Tips and Troubleshooting")

            TipItem(text = "If the other device cannot connect, make sure both devices are on the exact same network. Some routers isolate Wi-Fi clients from each other.")
            TipItem(text = "The server URL uses your local IP address. This changes when you reconnect to Wi-Fi, so always check the current URL after reconnecting.")
            TipItem(text = "For large file transfers, keep the screen on. The server runs in the foreground and will stop if the app is killed.")
            TipItem(text = "Use the PIN feature when sharing on public or shared networks to prevent unauthorized access.")
            TipItem(text = "The web UI supports dark mode. Tap the moon/sun icon in the top bar to toggle it.")
            TipItem(text = "QR codes are the fastest way to connect. Just scan and you are in - no typing needed.")

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun HowToCard(
    stepNumber: Int,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = iconColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SubStep(label: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TipCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "TIP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
