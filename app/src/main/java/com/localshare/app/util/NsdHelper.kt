package com.localshare.app.util

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.flow.asStateFlow

/**
 * Registers an mDNS/Bonjour service so devices on the local network
 * can reach the server via "localshare.local" instead of an IP address.
 *
 * Uses Android's built-in Network Service Discovery (NSD) API.
 */
object NsdHelper {

    private const val TAG = "NsdHelper"
    private const val SERVICE_NAME = "LocalShare"
    private const val SERVICE_TYPE = "_localshare._tcp."

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var isRegistered = false

    // Discovery properties
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private val devicesMap = java.util.concurrent.ConcurrentHashMap<String, com.localshare.app.server.DiscoveredDevice>()
    private val _discoveredDevices = kotlinx.coroutines.flow.MutableStateFlow<List<com.localshare.app.server.DiscoveredDevice>>(emptyList())
    val discoveredDevices: kotlinx.coroutines.flow.StateFlow<List<com.localshare.app.server.DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    /**
     * Register the LocalShare HTTP server as an mDNS service.
     */
    fun register(context: Context, port: Int, deviceName: String = SERVICE_NAME) {
        if (isRegistered) {
            Log.w(TAG, "mDNS service already registered, unregistering first")
            unregister()
        }

        try {
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = deviceName
                serviceType = SERVICE_TYPE
                setPort(port)
                setAttribute("app", "LocalShare")
            }

            val listener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(info: NsdServiceInfo) {
                    Log.i(TAG, "mDNS service registered: ${info.serviceName} on port $port")
                    isRegistered = true
                }
                override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "mDNS registration failed: errorCode=$errorCode")
                    isRegistered = false
                }
                override fun onServiceUnregistered(info: NsdServiceInfo) {
                    Log.i(TAG, "mDNS service unregistered: ${info.serviceName}")
                    isRegistered = false
                }
                override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "mDNS unregistration failed: errorCode=$errorCode")
                }
            }

            registrationListener = listener
            nsdManager = (context.getSystemService(Context.NSD_SERVICE) as NsdManager).also {
                it.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register mDNS service", e)
        }
    }

    /**
     * Unregister the mDNS service. Call this when the server stops.
     */
    fun unregister() {
        try {
            registrationListener?.let { listener ->
                nsdManager?.unregisterService(listener)
                Log.i(TAG, "mDNS service unregistration requested")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering mDNS service", e)
        } finally {
            registrationListener = null
            isRegistered = false
        }
    }

    /**
     * Start discovering LocalShare mDNS services.
     */
    fun startDiscovery(context: Context) {
        if (discoveryListener != null) return
        if (nsdManager == null) {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Service discovery started")
            }
            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType == SERVICE_TYPE) {
                    nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode")
                        }
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val ip = serviceInfo.host.hostAddress
                            if (ip != null && ip != "0.0.0.0") {
                                val device = com.localshare.app.server.DiscoveredDevice(
                                    alias = serviceInfo.serviceName,
                                    ip = ip,
                                    port = serviceInfo.port,
                                    lastSeen = System.currentTimeMillis()
                                )
                                devicesMap[ip] = device
                                _discoveredDevices.value = devicesMap.values.toList()
                            }
                        }
                    })
                }
            }
            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(TAG, "service lost: $service")
            }
            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
                stopDiscovery()
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code:$errorCode")
            }
        }

        try {
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start NSD discovery", e)
        }
    }

    /**
     * Stop discovering services.
     */
    fun stopDiscovery() {
        try {
            discoveryListener?.let {
                nsdManager?.stopServiceDiscovery(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping NSD discovery", e)
        } finally {
            discoveryListener = null
            devicesMap.clear()
            _discoveredDevices.value = emptyList()
        }
    }
}
