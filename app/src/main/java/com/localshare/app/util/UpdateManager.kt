package com.localshare.app.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

object UpdateManager {

    const val UPDATE_APK_FILENAME = "LocalShare_update.apk"

    fun canInstallFromThisContext(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Returns the cached downloaded APK file if it exists in Downloads, else null.
     */
    fun getCachedApkFile(context: Context): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, UPDATE_APK_FILENAME)
        return if (file.exists()) file else null
    }

    /**
     * Deletes the cached update APK to free space.
     */
    fun clearCachedApk(context: Context): Boolean {
        return getCachedApkFile(context)?.delete() == true
    }

    /**
     * Starts the system DownloadManager to fetch the APK.
     * Returns the download ID so the caller can track progress.
     */
    fun startDownload(context: Context, apkUrl: String): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Delete any existing cached APK first
        clearCachedApk(context)

        val uri = Uri.parse(apkUrl)
        val request = DownloadManager.Request(uri).apply {
            setTitle("AnyShare Update")
            setDescription("Downloading the latest version…")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, UPDATE_APK_FILENAME)
            setMimeType("application/vnd.android.package-archive")
        }
        return downloadManager.enqueue(request)
    }

    /**
     * Query download progress (0.0–1.0). Returns -1f on error.
     */
    fun getDownloadProgress(context: Context, downloadId: Long): Float {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = dm.query(query)
        if (cursor.moveToFirst()) {
            val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            cursor.close()
            return if (total > 0) downloaded.toFloat() / total.toFloat() else 0f
        }
        cursor.close()
        return -1f
    }

    /**
     * Registers a one-shot receiver that fires when the download completes,
     * then launches InstallActivity with a FileProvider URI.
     */
    fun registerCompletionReceiver(context: Context, downloadId: Long) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    launchInstaller(ctx, downloadId)
                    try { ctx.unregisterReceiver(this) } catch (_: Exception) {}
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    /**
     * Launches the installer using a FileProvider URI — the correct approach
     * for Android 8+ (fixes "app not installed" / "parse error" failures).
     */
    fun launchInstaller(context: Context, downloadId: Long) {
        if (!canInstallFromThisContext(context)) {
            Toast.makeText(context, "Allow installs from LocalShare in Settings first", Toast.LENGTH_LONG).show()
            requestInstallPermission(context)
            return
        }

        val apkFile = getCachedApkFile(context)
        if (apkFile == null) {
            // Fallback: try via DownloadManager URI
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = dm.getUriForDownloadedFile(downloadId)
            if (uri != null) {
                launchInstallerWithUri(context, uri)
            } else {
                Toast.makeText(context, "Download file not found. Try again.", Toast.LENGTH_LONG).show()
            }
            return
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )
        launchInstallerWithUri(context, contentUri)
    }

    private fun launchInstallerWithUri(context: Context, uri: Uri) {
        val installIntent = InstallActivity.createIntent(context, uri)
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open installer. Please enable 'Install unknown apps' in Settings.", Toast.LENGTH_LONG).show()
        }
    }
}
