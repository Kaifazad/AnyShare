package com.localshare.app.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.ConcurrentHashMap
import com.localshare.app.util.NetworkUtils

data class DiscoveredDevice(
    val alias: String,
    val ip: String,
    val port: Int,
    val lastSeen: Long
)

class DiscoveryListener(private val context: Context) {

    companion object {
        private const val TAG = "DiscoveryListener"
    }

    private var listenJob: Job? = null
    private var cleanupJob: Job? = null

    private val devicesMap = ConcurrentHashMap<String, DiscoveredDevice>()
    
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    fun start() {
        if (listenJob != null) return

        cleanupJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(5000) // Check every 5 seconds
                val now = System.currentTimeMillis()
                var removed = false
                
                val iterator = devicesMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (now - entry.value.lastSeen > 15000) { // 15s expiry
                        iterator.remove()
                        removed = true
                    }
                }
                
                if (removed) {
                    _discoveredDevices.value = devicesMap.values.toList()
                }
            }
        }

        listenJob = CoroutineScope(Dispatchers.IO).launch {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val multicastLock = wifiManager.createMulticastLock("LocalShareListenerLock")
            multicastLock.setReferenceCounted(true)
            multicastLock.acquire()

            var socket: MulticastSocket? = null
            try {
                // Bind to the correct network interface (handles hotspot/tethering)
                val localIp = NetworkUtils.getLocalIpAddress(context)
                val bindAddr = if (localIp != null && localIp != "0.0.0.0") {
                    java.net.InetSocketAddress(localIp, DiscoveryBroadcaster.MULTICAST_PORT)
                } else {
                    java.net.InetSocketAddress(DiscoveryBroadcaster.MULTICAST_PORT)
                }
                socket = MulticastSocket(bindAddr)
                socket.soTimeout = 0 // Wait indefinitely
                val groupAddress = InetAddress.getByName(DiscoveryBroadcaster.MULTICAST_GROUP)

                // Join group on the correct interface
                try {
                    val networkInterface = NetworkUtils.getWifiNetworkInterface(context)
                    if (networkInterface != null) {
                        socket.joinGroup(java.net.InetSocketAddress(groupAddress, DiscoveryBroadcaster.MULTICAST_PORT), networkInterface)
                    } else {
                        socket.joinGroup(groupAddress)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to join multicast on specific interface, trying default: ${e.message}")
                    socket.joinGroup(groupAddress)
                }

                val buffer = ByteArray(1024)

                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)

                    val payload = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    try {
                        val json = JSONObject(payload)
                        if (json.optString("app") == "LocalShare") {
                            val ip = json.getString("ip")
                            // Ignore self if possible, but IP might match. 
                            // That's fine, we can filter out self in the UI.
                            
                            val device = DiscoveredDevice(
                                alias = json.getString("alias"),
                                ip = ip,
                                port = json.getInt("port"),
                                lastSeen = System.currentTimeMillis()
                            )
                            
                            devicesMap[ip] = device
                            _discoveredDevices.value = devicesMap.values.toList()
                        }
                    } catch (e: Exception) {
                        // Invalid packet, ignore
                    }
                }
            } catch (e: CancellationException) {
                // Normal shutdown
            } catch (e: Exception) {
                Log.e(TAG, "Error in DiscoveryListener", e)
            } finally {
                try {
                    val groupAddress = InetAddress.getByName(DiscoveryBroadcaster.MULTICAST_GROUP)
                    val networkInterface = com.localshare.app.util.NetworkUtils.getWifiNetworkInterface(context)
                    if (networkInterface != null) {
                        @Suppress("DEPRECATION")
                        socket?.leaveGroup(java.net.InetSocketAddress(groupAddress, DiscoveryBroadcaster.MULTICAST_PORT), networkInterface)
                    } else {
                        @Suppress("DEPRECATION")
                        socket?.leaveGroup(groupAddress)
                    }
                } catch (e: Exception) {}
                socket?.close()
                if (multicastLock.isHeld) {
                    multicastLock.release()
                }
            }
        }
    }

    fun stop() {
        cleanupJob?.cancel()
        cleanupJob = null
        
        listenJob?.cancel()
        listenJob = null
        
        devicesMap.clear()
        _discoveredDevices.value = emptyList()
    }
}
