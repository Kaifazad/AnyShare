package com.localshare.app.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.localshare.app.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.UUID

class DiscoveryBroadcaster(private val context: Context) {

    companion object {
        private const val TAG = "DiscoveryBroadcaster"
        const val MULTICAST_GROUP = "224.0.0.167"
        const val MULTICAST_PORT = 53317
    }

    private var broadcastJob: Job? = null
    private val fingerprint = UUID.randomUUID().toString()

    fun start(alias: String, port: Int) {
        if (broadcastJob != null) return

        broadcastJob = CoroutineScope(Dispatchers.IO).launch {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val multicastLock = wifiManager.createMulticastLock("LocalShareBroadcasterLock")
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()

            var socket: MulticastSocket? = null
            try {
                // Bind to the correct network interface (handles hotspot/tethering)
                val localIp = NetworkUtils.getLocalIpAddress(context)
                if (localIp == null || localIp == "0.0.0.0") {
                    Log.e(TAG, "Cannot determine local IP, aborting broadcast")
                    return@launch
                }
                val bindAddr = java.net.InetSocketAddress(localIp, 0)
                socket = MulticastSocket(bindAddr)
                socket.timeToLive = 1 // Local network only

                val groupAddress = InetAddress.getByName(MULTICAST_GROUP)

                while (isActive) {
                    val ip = NetworkUtils.getLocalIpAddress(context) ?: "0.0.0.0"

                    val payload = JSONObject().apply {
                        put("app", "LocalShare")
                        put("alias", alias)
                        put("ip", ip)
                        put("port", port)
                        put("version", "1.0")
                        put("fingerprint", fingerprint)
                    }.toString().toByteArray(Charsets.UTF_8)

                    val packet = DatagramPacket(payload, payload.size, groupAddress, MULTICAST_PORT)
                    socket.send(packet)

                    delay(3000) // Broadcast every 3 seconds
                }
            } catch (e: CancellationException) {
                // Job cancelled, normal shutdown
            } catch (e: Exception) {
                Log.e(TAG, "Error in DiscoveryBroadcaster", e)
            } finally {
                socket?.close()
                if (multicastLock.isHeld) {
                    multicastLock.release()
                }
            }
        }
    }

    fun stop() {
        broadcastJob?.cancel()
        broadcastJob = null
    }
}
