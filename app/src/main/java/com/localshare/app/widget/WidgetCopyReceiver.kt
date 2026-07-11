package com.localshare.app.widget

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.localshare.app.service.ServerForegroundService

class WidgetCopyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.localshare.app.ACTION_COPY_URL") {
            val url = ServerForegroundService.serverUrl.value
            if (url != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("LocalShare URL", url)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "URL copied!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Server is not running", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
