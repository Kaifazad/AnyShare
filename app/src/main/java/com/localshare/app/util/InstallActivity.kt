package com.localshare.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

/**
 * Transparent trampoline Activity that launches the system package installer.
 * Receives a FileProvider content URI and fires ACTION_VIEW with it.
 * This is required on Android 8+ — the installer must be started from an
 * Activity context, not a Service or BroadcastReceiver.
 */
class InstallActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_APK_URI = "apk_uri"

        fun createIntent(context: Context, apkUri: Uri): Intent {
            return Intent(context, InstallActivity::class.java).apply {
                putExtra(EXTRA_APK_URI, apkUri.toString())
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uriString = intent.getStringExtra(EXTRA_APK_URI)
        if (uriString.isNullOrBlank()) {
            Toast.makeText(this, "Invalid APK path", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val apkUri = Uri.parse(uriString)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        try {
            startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Cannot open installer. Enable 'Install unknown apps' for AnyShare in Settings.",
                Toast.LENGTH_LONG
            ).show()
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
