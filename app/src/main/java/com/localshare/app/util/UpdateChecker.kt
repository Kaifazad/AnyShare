package com.localshare.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val releaseUrl: String,
    val description: String,
    val apkUrl: String?
)

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/Kaifazad/LocalShare/releases/latest"

    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                var latestVersion = json.getString("tag_name")
                if (latestVersion.startsWith("v", ignoreCase = true)) {
                    latestVersion = latestVersion.substring(1)
                }
                
                var currentVer = currentVersion
                if (currentVer.startsWith("v", ignoreCase = true)) {
                    currentVer = currentVer.substring(1)
                }

                var apkUrl: String? = null
                if (json.has("assets")) {
                    val assets = json.getJSONArray("assets")
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                }

                if (isNewerVersion(currentVer, latestVersion)) {
                    return@withContext UpdateInfo(
                        version = latestVersion,
                        releaseUrl = json.getString("html_url"),
                        description = json.optString("body", "A new version is available on GitHub."),
                        apkUrl = apkUrl
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
    
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        
        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
