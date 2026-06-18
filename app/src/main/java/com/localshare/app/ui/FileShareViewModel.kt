package com.localshare.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localshare.app.data.AccessLogEntry
import com.localshare.app.data.AppSettings
import com.localshare.app.data.ColorPalette
import com.localshare.app.data.FileCategory
import com.localshare.app.data.FileRepository
import com.localshare.app.data.ShareConfig
import com.localshare.app.data.SettingsRepository
import com.localshare.app.data.SharedFile
import com.localshare.app.data.ThemeMode
import com.localshare.app.service.ServerForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the entire app state: server lifecycle, file sharing config,
 * file listings, access logs, and app settings.
 */
class FileShareViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application)
    private val settingsRepository = SettingsRepository(application)

    // ─── Server State (from Service companion) ──────────────────

    val isServerRunning: StateFlow<Boolean> = ServerForegroundService.isRunning
    val serverUrl: StateFlow<String?> = ServerForegroundService.serverUrl
    val connectedDeviceCount: StateFlow<Int> = ServerForegroundService.connectedDeviceCount

    // ─── Share Config ───────────────────────────────────────────

    private val _shareConfig = MutableStateFlow(ShareConfig())
    val shareConfig: StateFlow<ShareConfig> = _shareConfig.asStateFlow()

    // ─── Access Logs ────────────────────────────────────────────

    private val _accessLogs = MutableStateFlow<List<AccessLogEntry>>(emptyList())
    val accessLogs: StateFlow<List<AccessLogEntry>> = _accessLogs.asStateFlow()

    // ─── App Settings ───────────────────────────────────────────

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Legacy compatibility — derived from settings
    val isDarkMode: StateFlow<Boolean?> get() {
        val mode = _appSettings.value.themeMode
        val value = when (mode) {
            ThemeMode.SYSTEM -> null
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        return MutableStateFlow(value)
    }

    init {
        // Load persisted settings
        _appSettings.value = settingsRepository.load()
        
        // Sync server settings
        syncServerSettings()

        // Periodically refresh logs
        viewModelScope.launch {
            while (true) {
                refreshLogs()
                kotlinx.coroutines.delay(3000)
            }
        }

        // Listen for new uploaded files from the server
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ServerForegroundService.newUploadedFiles.collect { file ->
                val uri = android.net.Uri.fromFile(file)
                fileRepository.resolveUris(listOf(uri)).firstOrNull()?.let { sharedFile ->
                    addSharedFiles(listOf(sharedFile))
                }
            }
        }

        // Clipboard sync: periodically push phone clipboard to the server
        viewModelScope.launch {
            val clipboard = getApplication<android.app.Application>()
                .getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            while (true) {
                try {
                    if (ServerForegroundService.isRunning.value) {
                        val clip = clipboard.primaryClip
                        if (clip != null && clip.itemCount > 0) {
                            val text = clip.getItemAt(0).text?.toString() ?: ""
                            ServerForegroundService.syncSystemClipboard(text)
                        }
                    }
                } catch (_: Exception) { }
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    // ─── Server Actions ─────────────────────────────────────────

    fun startServer() {
        ServerForegroundService.updateShareConfig(_shareConfig.value)
        syncServerSettings()
        ServerForegroundService.start(getApplication())
    }

    fun stopServer() {
        ServerForegroundService.stop(getApplication())
    }

    fun toggleServer() {
        if (isServerRunning.value) stopServer() else startServer()
    }

    // ─── Share Config Actions ───────────────────────────────────

    fun addSharedFiles(files: List<SharedFile>) {
        _shareConfig.value = _shareConfig.value.addFiles(files)
        syncShareConfig()
    }

    fun removeSharedFile(fileId: Long) {
        _shareConfig.value = _shareConfig.value.removeFile(fileId)
        syncShareConfig()
    }

    fun clearSharedFiles() {
        _shareConfig.value = _shareConfig.value.clear()
        syncShareConfig()
    }

    private fun syncShareConfig() {
        ServerForegroundService.updateShareConfig(_shareConfig.value)
    }

    // ─── Log Actions ────────────────────────────────────────────

    private fun refreshLogs() {
        val log = ServerForegroundService.accessLog
        _accessLogs.value = log?.getAll() ?: emptyList()
    }

    fun clearLogs() {
        ServerForegroundService.accessLog?.clear()
        _accessLogs.value = emptyList()
    }

    // ─── Settings Actions ───────────────────────────────────────

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val newSettings = transform(_appSettings.value)
        _appSettings.value = newSettings
        settingsRepository.save(newSettings)
        syncServerSettings()
    }

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun setColorPalette(palette: ColorPalette) = updateSettings { it.copy(colorPalette = palette) }

    fun setPin(pin: String?) = updateSettings { it.copy(pin = pin) }

    fun setDeviceName(name: String) = updateSettings { it.copy(deviceName = name) }

    fun setMaxConnections(max: Int) = updateSettings { it.copy(maxConnections = max.coerceIn(1, 5)) }

    fun setEnableNearbyDiscovery(enable: Boolean) = updateSettings { it.copy(enableNearbyDiscovery = enable) }

    fun randomizeDeviceName() {
        val name = SettingsRepository.generateCuteName()
        setDeviceName(name)
    }

    // Legacy compatibility
    fun setDarkMode(isDark: Boolean?) {
        val mode = when (isDark) {
            null -> ThemeMode.SYSTEM
            true -> ThemeMode.DARK
            false -> ThemeMode.LIGHT
        }
        setThemeMode(mode)
    }

    private fun syncServerSettings() {
        val settings = _appSettings.value
        ServerForegroundService.updateServerSettings(
            pin = settings.pin,
            deviceName = settings.deviceName,
            maxConnections = settings.maxConnections,
            enableNearbyDiscovery = settings.enableNearbyDiscovery
        )
    }
}
