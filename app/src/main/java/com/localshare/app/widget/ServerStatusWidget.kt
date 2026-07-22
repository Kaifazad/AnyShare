package com.localshare.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.localshare.app.R
import com.localshare.app.service.ServerForegroundService

class ServerStatusWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE = "com.localshare.app.WIDGET_TOGGLE"

        fun updateAllWidgets(context: Context) {
            try {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, ServerStatusWidget::class.java))
                if (ids.isNotEmpty()) {
                    val intent = Intent(context, ServerStatusWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) updateOne(context, mgr, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            try {
                if (ServerForegroundService.isRunning.value) {
                    ServerForegroundService.stop(context)
                } else {
                    ServerForegroundService.start(context)
                }
            } catch (_: Exception) {}
            updateAllWidgets(context)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                updateAllWidgets(context)
            }, 700)
        }
    }

    private fun updateOne(context: Context, mgr: AppWidgetManager, id: Int) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_server_status)
            val running = try { ServerForegroundService.isRunning.value } catch (_: Exception) { false }
            val url = try { ServerForegroundService.serverUrl.value } catch (_: Exception) { null }

            // Circle: green when running, red when stopped
            views.setImageViewResource(R.id.status_indicator,
                if (running) R.drawable.widget_status_circle else R.drawable.widget_status_circle_offline)

            // Status text
            views.setTextViewText(R.id.status_text,
                if (running && url != null) url else if (running) "Starting..." else "Offline")

            // Button icon
            views.setImageViewResource(R.id.toggle_button,
                if (running) R.drawable.widget_toggle_stop else R.drawable.widget_toggle_play)

            // Button background: slightly brighter when running
            views.setImageViewResource(R.id.toggle_button,
                if (running) R.drawable.widget_toggle_stop else R.drawable.widget_toggle_play)

            val toggle = PendingIntent.getBroadcast(context, 0,
                Intent(context, ServerStatusWidget::class.java).apply { action = ACTION_TOGGLE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.toggle_button, toggle)
            views.setOnClickPendingIntent(R.id.widget_container, toggle)

            val launch = PendingIntent.getActivity(context, 1,
                Intent(context, com.localshare.app.ui.FileShareActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.app_title, launch)
            views.setOnClickPendingIntent(R.id.status_text, launch)

            mgr.updateAppWidget(id, views)
        } catch (_: Exception) {}
    }
}
