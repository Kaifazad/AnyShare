package com.localshare.app.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class InstallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DOWNLOAD_ID = "download_id"

        fun createIntent(context: Context, downloadId: Long): Intent {
            return Intent(context, InstallActivity::class.java).apply {
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
        if (downloadId == -1L) {
            Toast.makeText(this, "Invalid download", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val uri = downloadManager.getUriForDownloadedFile(downloadId)

        if (uri == null) {
            Toast.makeText(this, "Download not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Launch package installer from Activity context
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        try {
            startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot install APK. Please enable 'Install unknown apps' in Settings.", Toast.LENGTH_LONG).show()
            // Open app settings so user can enable it
            try {
                val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(settingsIntent)
            } catch (_: Exception) {}
        }

        finish()
    }
}
