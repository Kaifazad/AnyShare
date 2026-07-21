package com.localshare.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.localshare.app.data.AccessLogEntry
import com.localshare.app.data.AppSettings
import com.localshare.app.data.ColorPalette
import com.localshare.app.data.CrashReport
import com.localshare.app.data.CrashRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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
    
    val activeDownloads: StateFlow<List<com.localshare.app.server.FileShareServer.ActiveDownload>> = ServerForegroundService.activeDownloads
    val connectedClients: StateFlow<List<com.localshare.app.server.FileShareServer.ConnectedClient>> = ServerForegroundService.connectedClients

    // ─── Share Config ───────────────────────────────────────────

    private val _shareConfig = MutableStateFlow(ShareConfig())
    val shareConfig: StateFlow<ShareConfig> = _shareConfig.asStateFlow()

    // ─── Multi-Select State ─────────────────────────────────────

    private val _selectedFileIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedFileIds: StateFlow<Set<Long>> = _selectedFileIds.asStateFlow()

    val isMultiSelectMode: StateFlow<Boolean> = _selectedFileIds.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Pending uploads from web UI (not yet accepted)
    private val _pendingUploads = MutableStateFlow<List<SharedFile>>(emptyList())
    val pendingUploads: StateFlow<List<SharedFile>> = _pendingUploads.asStateFlow()

    // ─── Access Logs ────────────────────────────────────────────

    private val _accessLogs = MutableStateFlow<List<AccessLogEntry>>(emptyList())
    val accessLogs: StateFlow<List<AccessLogEntry>> = _accessLogs.asStateFlow()

    // ─── App Settings ───────────────────────────────────────────

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _settingsLoaded = MutableStateFlow(false)
    val settingsLoaded: StateFlow<Boolean> = _settingsLoaded.asStateFlow()

    // ─── Crash Reports ──────────────────────────────────────────

    private val crashRepository = CrashRepository(application)
    private val _crashReports = MutableStateFlow<List<CrashReport>>(emptyList())
    val crashReports: StateFlow<List<CrashReport>> = _crashReports.asStateFlow()
    
    // ─── App Updates ────────────────────────────────────────────

    enum class UpdateStatus { IDLE, CHECKING, UP_TO_DATE, UPDATE_AVAILABLE, ERROR }

    private val _updateInfo = MutableStateFlow<com.localshare.app.util.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.localshare.app.util.UpdateInfo?> = _updateInfo.asStateFlow()

    private val _updateStatus = MutableStateFlow(UpdateStatus.IDLE)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.CHECKING
            try {
                val info = com.localshare.app.util.UpdateChecker.checkForUpdate(currentVersion)
                if (info != null) {
                    _updateInfo.value = info
                    _updateStatus.value = UpdateStatus.UPDATE_AVAILABLE
                } else {
                    _updateStatus.value = UpdateStatus.UP_TO_DATE
                }
            } catch (e: Exception) {
                _updateStatus.value = UpdateStatus.ERROR
            }
        }
    }

    // ─── Incoming Transfer Sessions ────────────────────────────

    private val _incomingTransfer = MutableStateFlow<com.localshare.app.data.TransferSession?>(null)
    val incomingTransfer: StateFlow<com.localshare.app.data.TransferSession?> = _incomingTransfer.asStateFlow()

    fun acceptTransfer(sessionId: String) {
        viewModelScope.launch {
            val accepted = com.localshare.app.service.ServerForegroundService.acceptTransfer(sessionId)
            if (accepted) {
                _incomingTransfer.value = _incomingTransfer.value?.copy(
                    status = com.localshare.app.data.SessionStatus.ACTIVE
                )
            }
        }
    }

    fun rejectTransfer(sessionId: String) {
        viewModelScope.launch {
            com.localshare.app.service.ServerForegroundService.rejectTransfer(sessionId)
            _incomingTransfer.value = null
        }
    }

    fun dismissIncomingTransfer() {
        _incomingTransfer.value = null
    }

    // Legacy compatibility — derived from settings
    val isDarkMode: StateFlow<Boolean?> = _appSettings.map { settings ->
        when (settings.themeMode) {
            ThemeMode.SYSTEM -> null
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Load persisted settings
            val initialSettings = settingsRepository.load()
            _appSettings.value = initialSettings
            _settingsLoaded.value = true

            // Load crash reports
            _crashReports.value = crashRepository.loadAll()
            
            // Sync server settings
            syncServerSettings()

            // Sync haptic setting
            com.localshare.app.ui.utils.HapticHelper.enabled = initialSettings.hapticEnabled
        }

        // Periodically refresh logs
        viewModelScope.launch {
            while (true) {
                refreshLogs()
                kotlinx.coroutines.delay(3000)
            }
        }

        // Listen for incoming transfer sessions from other devices
        viewModelScope.launch {
            com.localshare.app.service.ServerForegroundService.incomingTransfers.collect { session ->
                _incomingTransfer.value = session
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

        // Listen for remote clear commands
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            ServerForegroundService.clearFilesEvent.collect {
                clearSharedFiles()
            }
        }

        // Clipboard sync: periodically push phone clipboard to the server
        viewModelScope.launch {
            val clipboard = getApplication<android.app.Application>()
                .getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            while (true) {
                try {
                    if (ServerForegroundService.isRunning.value && _appSettings.value.clipboardSyncEnabled) {
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

    fun toggleFileSelection(fileId: Long) {
        _selectedFileIds.value = _selectedFileIds.value.let { selected ->
            if (fileId in selected) selected - fileId else selected + fileId
        }
    }

    fun selectAllFiles() {
        _selectedFileIds.value = _shareConfig.value.sharedFiles.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedFileIds.value = emptySet()
    }

    fun removeSelectedFiles() {
        val ids = _selectedFileIds.value
        _shareConfig.value = _shareConfig.value.removeFiles(ids)
        _selectedFileIds.value = emptySet()
        syncShareConfig()
    }

    fun getFileById(id: Long): SharedFile? {
        return _shareConfig.value.sharedFiles.find { it.id == id }
    }

    private fun syncShareConfig() {
        ServerForegroundService.updateShareConfig(_shareConfig.value)
    }

    // ─── Pending Upload Actions ────────────────────────────────

    fun acceptUpload(file: SharedFile) {
        _pendingUploads.value = _pendingUploads.value.filter { it.id != file.id }
        addSharedFiles(listOf(file))
    }

    fun removePendingUpload(file: SharedFile) {
        _pendingUploads.value = _pendingUploads.value.filter { it.id != file.id }
    }

    fun clearPendingUploads() {
        _pendingUploads.value = emptyList()
    }

    fun saveUploadedFile(file: SharedFile) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val pendingFile = java.io.File(file.path)
                if (!pendingFile.exists()) return@launch

                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val localShareDir = java.io.File(downloadsDir, "LocalShare")
                if (!localShareDir.exists()) localShareDir.mkdirs()

                val destFile = java.io.File(localShareDir, file.name)
                val finalDest = if (destFile.exists()) {
                    val base = destFile.nameWithoutExtension
                    val ext = destFile.extension
                    var counter = 1
                    var candidate: java.io.File
                    do {
                        candidate = java.io.File(localShareDir, "${base}_${counter}.${ext}")
                        counter++
                    } while (candidate.exists())
                    candidate
                } else destFile

                pendingFile.copyTo(finalDest, overwrite = true)
                android.media.MediaScannerConnection.scanFile(
                    getApplication(),
                    arrayOf(finalDest.absolutePath),
                    null,
                    null
                )
            } catch (e: Exception) {
                android.util.Log.e("FileShareViewModel", "Error saving uploaded file", e)
            }
        }
    }

    private fun refreshLogs() {
        val log = ServerForegroundService.accessLog
        _accessLogs.value = log?.getAll() ?: emptyList()
    }

    fun clearLogs() {
        ServerForegroundService.accessLog?.clear()
        _accessLogs.value = emptyList()
    }

    fun clearCrashReports() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            crashRepository.clear()
            _crashReports.value = emptyList()
        }
    }

    // ─── Settings Actions ───────────────────────────────────────

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val newSettings = transform(_appSettings.value)
        _appSettings.value = newSettings
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            settingsRepository.save(newSettings)
        }
        syncServerSettings()
        com.localshare.app.ui.utils.HapticHelper.enabled = newSettings.hapticEnabled
    }

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun setColorPalette(palette: ColorPalette) = updateSettings { it.copy(colorPalette = palette) }

    fun setPin(pin: String?) = updateSettings { it.copy(pin = pin) }

    fun setDeviceName(name: String) = updateSettings { it.copy(deviceName = name) }

    fun setMaxConnections(max: Int) = updateSettings { it.copy(maxConnections = max.coerceIn(1, 5)) }



    fun setAmoledMode(enabled: Boolean) = updateSettings { it.copy(amoledMode = enabled) }

    fun setHapticEnabled(enabled: Boolean) = updateSettings { it.copy(hapticEnabled = enabled) }

    fun setThemeColorSeed(seed: String) = updateSettings { it.copy(themeColorSeed = seed, colorPalette = ColorPalette.SYSTEM) }

    fun setEncryptionEnabled(enabled: Boolean) = updateSettings { it.copy(encryptionEnabled = enabled) }

    fun setClipboardSyncEnabled(enabled: Boolean) = updateSettings { it.copy(clipboardSyncEnabled = enabled) }

    fun completeOnboarding() = updateSettings { it.copy(onboardingCompleted = true) }

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
            encryptionEnabled = settings.encryptionEnabled
        )
    }
}
