package com.localshare.app.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class RemoteFile(
    val id: String,
    val name: String,
    val size: String,
    val type: String,
    val downloadUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveBottomSheet(initialUrl: String? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    var serverUrl by remember { mutableStateOf(initialUrl ?: "") }
    var remoteFiles by remember { mutableStateOf<List<RemoteFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    val connectToServer = { urlToConnect: String ->
        val url = if (urlToConnect.endsWith("/")) urlToConnect.dropLast(1) else urlToConnect
        val finalUrl = if (!url.startsWith("http")) "http://$url" else url
        serverUrl = finalUrl
        
        isLoading = true
        fetchFiles(finalUrl) { files, error ->
            isLoading = false
            if (error == "401") {
                showPinDialog = true
            } else if (error != null) {
                errorMsg = error
                isConnected = false
            } else {
                remoteFiles = files
                isConnected = true
                errorMsg = null
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrEmpty()) {
            connectToServer(initialUrl)
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            connectToServer(result.contents)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isConnected) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Connect to Sender",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Scan the QR code on the sender's device or enter their IP address.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        options.setPrompt("Scan Sender's QR Code")
                        options.setBeepEnabled(true)
                        options.setCaptureActivity(com.localshare.app.ui.components.PortraitCaptureActivity::class.java)
                        options.setOrientationLocked(true)
                        scanLauncher.launch(options)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code", style = MaterialTheme.typography.titleMedium)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL (e.g. http://192.168.1.5:8080)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        connectToServer(serverUrl)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Connect")
                    }
                }
                
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = errorMsg!!, color = MaterialTheme.colorScheme.error)
                }

                if (showPinDialog) {
                    AlertDialog(
                        onDismissRequest = { showPinDialog = false },
                        title = { Text("PIN Required") },
                        text = {
                            Column {
                                Text("Enter the 4-digit PIN to connect.")
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { pinInput = it },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showPinDialog = false
                                isLoading = true
                                authenticateAndFetch(serverUrl, pinInput) { files, error ->
                                    isLoading = false
                                    if (error != null) {
                                        errorMsg = error
                                        isConnected = false
                                    } else {
                                        remoteFiles = files
                                        isConnected = true
                                        errorMsg = null
                                    }
                                }
                            }) {
                                Text("Submit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPinDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                
            } else {
                // Connected State
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Connected to", style = MaterialTheme.typography.bodySmall)
                            Text(serverUrl, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { isConnected = false; serverUrl = "" }) {
                            Text("Disconnect")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (remoteFiles.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No files shared", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(remoteFiles) { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.name,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = file.size,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    val interactionSource = remember { MutableInteractionSource() }
                                    IconButton(
                                        onClick = {
                                            downloadFile(context, file.downloadUrl, file.name)
                                        },
                                        modifier = Modifier.bounceScale(interactionSource),
                                        interactionSource = interactionSource,
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.Filled.Download,
                                            contentDescription = "Download",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun fetchFiles(baseUrl: String, onResult: (List<RemoteFile>, String?) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("$baseUrl/api/files")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonObject = JSONObject(response)
                val jsonArray = jsonObject.getJSONArray("files")
                val files = mutableListOf<RemoteFile>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    files.add(
                        RemoteFile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            size = obj.getString("formattedSize"),
                            type = obj.getString("typeIcon"),
                            downloadUrl = "$baseUrl/download/${obj.getString("id")}"
                        )
                    )
                }
                withContext(Dispatchers.Main) { onResult(files, null) }
            } else if (connection.responseCode == 401) {
                withContext(Dispatchers.Main) { onResult(emptyList(), "401") }
            } else {
                withContext(Dispatchers.Main) { onResult(emptyList(), "Server returned ${connection.responseCode}") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(emptyList(), "Failed to connect: ${e.localizedMessage}") }
        }
    }
}

private fun authenticateAndFetch(baseUrl: String, pin: String, onResult: (List<RemoteFile>, String?) -> Unit) {
    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = URL("$baseUrl/api/auth")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val jsonBody = JSONObject().apply { put("pin", pin) }.toString()
            connection.outputStream.write(jsonBody.toByteArray())
            
            if (connection.responseCode == 200) {
                // Auth successful, fetch files again
                fetchFiles(baseUrl, onResult)
            } else {
                withContext(Dispatchers.Main) { onResult(emptyList(), "Incorrect PIN or Server Error") }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(emptyList(), "Failed to connect: ${e.localizedMessage}") }
        }
    }
}

private fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading from LocalShare")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LocalShare/$fileName")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        Toast.makeText(context, "Downloading $fileName...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
