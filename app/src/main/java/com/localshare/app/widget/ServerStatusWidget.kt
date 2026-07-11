package com.localshare.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.localshare.app.R
import com.localshare.app.service.ServerForegroundService

class ServerStatusWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_SERVER = "com.localshare.app.ACTION_TOGGLE_SERVER"
        private const val ACTION_COPY_URL = "com.localshare.app.ACTION_COPY_URL"

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, ServerStatusWidget::class.java)
                )
                if (widgetIds.isNotEmpty()) {
                    val intent = Intent(context, ServerStatusWidget::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (_: Exception) {}
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_SERVER) {
            try {
                if (ServerForegroundService.isRunning.value) {
                    ServerForegroundService.stop(context)
                } else {
                    ServerForegroundService.start(context)
                }
            } catch (_: Exception) {}

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                updateAllWidgets(context)
            }, 600)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        try {
            val views = RemoteViews(context.packageName, R.layout.widget_server_status)

            val isRunning = try {
                ServerForegroundService.isRunning.value
            } catch (_: Exception) {
                false
            }

            val serverUrl = try {
                ServerForegroundService.serverUrl.value
            } catch (_: Exception) {
                null
            }

            // Status dot
            views.setImageViewResource(
                R.id.status_indicator,
                if (isRunning) R.drawable.widget_status_dot_online
                else R.drawable.widget_status_dot_offline
            )

            // Status text
            views.setTextViewText(
                R.id.status_text,
                if (isRunning) "Server running" else "Tap to start"
            )

            // Toggle icon
            views.setImageViewResource(
                R.id.toggle_button,
                if (isRunning) R.drawable.widget_toggle_stop
                else R.drawable.widget_toggle_play
            )

            // URL row visibility
            if (isRunning && serverUrl != null) {
                views.setViewVisibility(R.id.url_row, View.VISIBLE)
                views.setTextViewText(R.id.url_text, serverUrl)
            } else {
                views.setViewVisibility(R.id.url_row, View.GONE)
            }

            // Toggle button click
            val toggleIntent = Intent(context, ServerStatusWidget::class.java).apply {
                action = ACTION_TOGGLE_SERVER
            }
            val togglePending = PendingIntent.getBroadcast(
                context, 0, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.toggle_button, togglePending)

            // Copy button click
            val copyIntent = Intent(context, WidgetCopyReceiver::class.java).apply {
                action = ACTION_COPY_URL
            }
            val copyPending = PendingIntent.getBroadcast(
                context, 2, copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.copy_button, copyPending)

            // Tap container to toggle
            views.setOnClickPendingIntent(R.id.widget_container, togglePending)

            // Tap app name to open app
            val launchIntent = Intent(context, com.localshare.app.ui.FileShareActivity::class.java)
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val launchPending = PendingIntent.getActivity(
                context, 1, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.app_title, launchPending)

            appWidgetManager.updateAppWidget(widgetId, views)
        } catch (_: Exception) {}
    }
}
