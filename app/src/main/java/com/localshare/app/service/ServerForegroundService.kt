package com.localshare.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.localshare.app.data.AccessLogBuffer
import com.localshare.app.data.ShareConfig
import com.localshare.app.server.FileShareServer
import com.localshare.app.ui.FileShareActivity
import com.localshare.app.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the HTTP server running while the app is in the background.
 * Shows a persistent notification with server URL and a Stop action.
 */
class ServerForegroundService : Service() {

    companion object {
        private const val TAG = "ServerService"
        private const val CHANNEL_ID = "localshare_server"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "com.localshare.app.ACTION_STOP_SERVER"
        private var currentPort = 8080

        // Shared state accessible from ViewModel
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _serverUrl = MutableStateFlow<String?>(null)
        val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

        private val _connectedDeviceCount = MutableStateFlow(0)
        val connectedDeviceCount: StateFlow<Int> = _connectedDeviceCount.asStateFlow()

        private var _server: FileShareServer? = null
        val accessLog: AccessLogBuffer? get() = _server?.accessLog

        // Server settings from app settings
        private var _pin: String? = null
        private var _deviceName: String = "LocalShare"
        private var _maxConnections: Int = 3

        fun updateShareConfig(config: ShareConfig) {
            _server?.shareConfig = config
        }

        /**
         * Update server settings (PIN, device name, max connections) at runtime.
         */
        fun updateServerSettings(pin: String?, deviceName: String, maxConnections: Int) {
            _pin = pin
            _deviceName = deviceName
            _maxConnections = maxConnections
            // If server is already running, update it live
            _server?.let { server ->
                server.pin = pin
                server.deviceName = deviceName
                server.maxConnections = maxConnections
            }
        }

        fun start(context: Context) {
            val intent = Intent(context, ServerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ServerForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private var server: FileShareServer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startServer()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopServer()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startServer() {
        try {
            val ip = NetworkUtils.getLocalIpAddress(this)
            
            var bound = false
            var portToTry = 8080
            
            while (!bound && portToTry <= 8090) {
                try {
                    server = FileShareServer(this, portToTry).also {
                        // Apply settings before starting
                        it.pin = _pin
                        it.deviceName = _deviceName
                        it.maxConnections = _maxConnections
                        it.start()
                        _server = it
                    }
                    bound = true
                    currentPort = portToTry
                } catch (e: java.net.BindException) {
                    portToTry++
                }
            }
            
            if (!bound) {
                throw Exception("Could not find an open port between 8080 and 8090")
            }

            val url = "http://$ip:$currentPort"

            _isRunning.value = true
            _serverUrl.value = url

            // Observe connected device count
            serviceScope.launch {
                server?.connectedDeviceCount?.collect { count ->
                    _connectedDeviceCount.value = count
                }
            }

            // Start foreground with notification
            val notification = buildNotification(url)
            startForeground(NOTIFICATION_ID, notification)

            Log.i(TAG, "Server started at $url")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
            
            // CRITICAL: We MUST call startForeground even if it fails, otherwise Android throws
            // a RemoteServiceException (ForegroundServiceDidNotStartInTimeException) and crashes the app.
            val errorNotification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Failed to start server")
                .setContentText("Check your network connection.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .build()
            startForeground(NOTIFICATION_ID, errorNotification)
            
            _isRunning.value = false
            stopSelf()
        }
    }

    private fun stopServer() {
        try {
            server?.stop()
            server = null
            _server = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server", e)
        }

        _isRunning.value = false
        _serverUrl.value = null
        _connectedDeviceCount.value = 0

        Log.i(TAG, "Server stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LocalShare Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when LocalShare server is running"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(url: String): Notification {
        // Open app intent
        val openIntent = Intent(this, FileShareActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Stop action intent
        val stopIntent = Intent(this, ServerForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("${_deviceName} is sharing")
            .setContentText(url)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    null, "Stop Server", stopPendingIntent
                ).build()
            )
            .build()
    }
}
