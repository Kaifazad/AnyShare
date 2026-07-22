package com.localshare.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

/**
 * Utility to detect the device's local IP address.
 * Handles Wi-Fi, hotspot/tethering, and Ethernet interfaces.
 */
object NetworkUtils {

    /**
     * Get the device's local IP address on the active network.
     * Handles Wi-Fi, hotspot (tethering), USB tethering, and Ethernet.
     */
    fun getLocalIpAddress(context: Context): String {
        // 1. Try WifiManager first (works for Wi-Fi connected mode)
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            val wifiInfo = wifiManager?.connectionInfo
            @Suppress("DEPRECATION")
            val ipInt = wifiInfo?.ipAddress ?: 0

            if (ipInt != 0) {
                val ip = String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
                if (ip != "0.0.0.0") return ip
            }
        } catch (_: Exception) { }

        // 2. Fallback: enumerate ALL network interfaces
        //    This catches hotspot (wlan1/ap0), USB tethering (rndis0),
        //    Ethernet (eth0), and any other non-loopback interface.
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            // Collect all valid non-loopback IPv4 addresses
            val candidates = mutableListOf<Pair<String, Int>>() // (ip, interface priority)

            for (networkInterface in interfaces) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue

                val name = networkInterface.name.lowercase()
                // Priority: hotspot/tethering interfaces first
                val priority = when {
                    name.contains("ap") || name.contains("wlan1") -> 100  // hotspot
                    name.contains("rndis") || name.contains("usb") -> 90  // USB tethering
                    name.contains("tether") -> 85                          // tethering
                    name.contains("wlan") -> 70                            // Wi-Fi
                    name.contains("eth") -> 60                             // Ethernet
                    name.contains("rmnet") -> 50                           // cellular
                    else -> 10                                              // anything else
                }

                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (address is Inet4Address && !address.isLoopbackAddress) {
                        val hostAddress = address.hostAddress
                        if (hostAddress != null && hostAddress != "0.0.0.0") {
                            candidates.add(hostAddress to priority)
                        }
                    }
                }
            }

            // Return the highest-priority IP
            if (candidates.isNotEmpty()) {
                candidates.sortByDescending { it.second }
                return candidates.first().first
            }
        } catch (_: Exception) { }

        return "0.0.0.0"
    }

    /**
     * Finds the NetworkInterface associated with the current active network.
     * Works for both Wi-Fi and hotspot modes.
     */
    fun getWifiNetworkInterface(context: Context): NetworkInterface? {
        val ip = getLocalIpAddress(context)
        if (ip == "0.0.0.0") return null
        try {
            val inetAddr = InetAddress.getByName(ip)
            return NetworkInterface.getByInetAddress(inetAddr)
        } catch (_: Exception) {}
        return null
    }

    /**
     * Check if the device is connected to a Wi-Fi or hotspot network.
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
