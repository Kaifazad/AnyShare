package com.localshare.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.util.Log
import com.localshare.app.data.SessionStatus
import com.localshare.app.data.db.AppDatabase
import com.localshare.app.server.FileShareServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val sessionId = intent.getStringExtra("EXTRA_SESSION_ID") ?: return
        val notificationId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", sessionId.hashCode())

        Log.d("TransferActionReceiver", "Received action $action for session $sessionId")

        // Dismiss the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update active session in memory (Server)
                val server = com.localshare.app.service.ServerForegroundService.server
                val session = server?.activeSessions?.get(sessionId)
                
                if (session != null) {
                    when (action) {
                        "ACTION_ACCEPT_TRANSFER" -> {
                            server.activeSessions[sessionId] = session.copy(status = SessionStatus.ACTIVE)
                            Log.d("TransferActionReceiver", "Session $sessionId ACCEPTED")
                        }
                        "ACTION_REJECT_TRANSFER" -> {
                            server.activeSessions[sessionId] = session.copy(status = SessionStatus.REJECTED)
                            Log.d("TransferActionReceiver", "Session $sessionId REJECTED")
                        }
                    }
                }
                
                // (Optional) Update in Room Database if Receiver stores sessions
            } catch (e: Exception) {
                Log.e("TransferActionReceiver", "Error handling transfer action", e)
            }
        }
    }
}
