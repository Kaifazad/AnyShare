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

    // ─── File Listings ──────────────────────────────────────────

    private val _allFiles = MutableStateFlow<Map<FileCategory, List<SharedFile>>>(emptyMap())
    val allFiles: StateFlow<Map<FileCategory, List<SharedFile>>> = _allFiles.asStateFlow()

    private val _isLoadingFiles = MutableStateFlow(false)
    val isLoadingFiles: StateFlow<Boolean> = _isLoadingFiles.asStateFlow()

    // ─── Access Logs ────────────────────────────────────────────

    private val _accessLogs = MutableStateFlow<List<AccessLogEntry>>(emptyList())
    val accessLogs: StateFlow<List<AccessLogEntry>> = _accessLogs.asStateFlow()

    // ─── Search ─────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FileCategory?>(null)
    val selectedCategory: StateFlow<FileCategory?> = _selectedCategory.asStateFlow()

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

        loadFiles()
        // Periodically refresh logs
        viewModelScope.launch {
            while (true) {
                refreshLogs()
                kotlinx.coroutines.delay(3000)
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

    fun toggleCategory(category: FileCategory) {
        _shareConfig.value = _shareConfig.value.toggleCategory(category)
        syncShareConfig()
    }

    fun toggleFile(fileId: Long) {
        _shareConfig.value = _shareConfig.value.toggleFile(fileId)
        syncShareConfig()
    }

    fun selectAllInCategory(category: FileCategory) {
        val files = _allFiles.value[category] ?: return
        var config = _shareConfig.value
        for (file in files) {
            if (!config.isFileSelected(file.id)) {
                config = config.addFile(file.id)
            }
        }
        _shareConfig.value = config
        syncShareConfig()
    }

    fun deselectAllInCategory(category: FileCategory) {
        val files = _allFiles.value[category] ?: return
        var config = _shareConfig.value
        for (file in files) {
            config = config.removeFile(file.id)
        }
        _shareConfig.value = config
        syncShareConfig()
    }

    private fun syncShareConfig() {
        ServerForegroundService.updateShareConfig(_shareConfig.value)
    }

    fun addCustomFolder(uriString: String) {
        _shareConfig.value = _shareConfig.value.addCustomFolder(uriString)
        syncShareConfig()
        loadFiles()
    }

    fun removeCustomFolder(uriString: String) {
        _shareConfig.value = _shareConfig.value.removeCustomFolder(uriString)
        syncShareConfig()
        loadFiles()
    }

    // ─── File Actions ───────────────────────────────────────────

    fun loadFiles() {
        viewModelScope.launch {
            _isLoadingFiles.value = true
            try {
                _allFiles.value = fileRepository.getAllFiles(_shareConfig.value.customFolderUris)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingFiles.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: FileCategory?) {
        _selectedCategory.value = category
    }

    fun getFilteredFiles(): List<SharedFile> {
        val category = _selectedCategory.value
        val query = _searchQuery.value.lowercase()

        val files = if (category != null) {
            _allFiles.value[category] ?: emptyList()
        } else {
            _allFiles.value.values.flatten()
        }

        return if (query.isBlank()) {
            files
        } else {
            files.filter { it.name.lowercase().contains(query) }
        }
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
            maxConnections = settings.maxConnections
        )
    }
}
