package com.localshare.app.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast

object UpdateManager {

    fun downloadAndInstallUpdate(context: Context, apkUrl: String) {
        Toast.makeText(context, "Downloading update... See notifications for progress", Toast.LENGTH_LONG).show()

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(apkUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle("LocalShare Update")
            setDescription("Downloading the latest version...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "LocalShare_update.apk")
            setMimeType("application/vnd.android.package-archive")
        }

        val downloadId = downloadManager.enqueue(request)

        // Register a receiver to automatically launch the installer when finished
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    val downloadedUri = downloadManager.getUriForDownloadedFile(downloadId)
                    if (downloadedUri != null) {
                        installIntent.setDataAndType(downloadedUri, "application/vnd.android.package-archive")
                        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        try {
                            context.startActivity(installIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Failed to start installation.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // Unregister to prevent leaks
                    try {
                        context.unregisterReceiver(this)
                    } catch (e: Exception) {}
                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
}
