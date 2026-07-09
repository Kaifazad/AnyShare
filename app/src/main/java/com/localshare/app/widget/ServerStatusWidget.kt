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
        private const val ACTION_TOGGLE_SERVER = "com.localshare.app.ACTION_TOGGLE_SERVER"

        fun updateWidget(context: Context) {
            val intent = Intent(context, ServerStatusWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, ServerStatusWidget::class.java)
                )
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            }
            context.sendBroadcast(intent)
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
            if (ServerForegroundService.isRunning.value) {
                ServerForegroundService.stop(context)
            } else {
                ServerForegroundService.start(context)
            }

            // Update all widgets after toggle
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ServerStatusWidget::class.java)
            )
            for (widgetId in widgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_server_status)

        val isRunning = ServerForegroundService.isRunning.value

        // Update status indicator
        views.setImageViewResource(
            R.id.status_indicator,
            if (isRunning) R.drawable.widget_status_dot_online
            else R.drawable.widget_status_dot_offline
        )

        // Update status text
        views.setTextViewText(
            R.id.status_text,
            if (isRunning) "Server Running" else "Server Offline"
        )

        // Update toggle button icon
        views.setImageViewResource(
            R.id.toggle_button,
            if (isRunning) R.drawable.widget_toggle_stop
            else R.drawable.widget_toggle_play
        )

        // Set up toggle button click
        val toggleIntent = Intent(context, ServerStatusWidget::class.java).apply {
            action = ACTION_TOGGLE_SERVER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.toggle_button, pendingIntent)

        // Clicking the widget body also opens the app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val launchPending = PendingIntent.getActivity(
            context, 1, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.status_text, launchPending)
        views.setOnClickPendingIntent(R.id.app_title, launchPending)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
