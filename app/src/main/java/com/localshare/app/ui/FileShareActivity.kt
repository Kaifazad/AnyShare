package com.localshare.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.compose.foundation.isSystemInDarkTheme
import com.localshare.app.data.CrashReport
import com.localshare.app.data.CrashRepository
import com.localshare.app.data.ThemeMode
import com.localshare.app.ui.screens.OnboardingScreen
import com.localshare.app.ui.screens.PermissionsScreen
import com.localshare.app.ui.screens.IncomingTransferDialog
import com.localshare.app.ui.theme.LocalShareTheme

/**
 * Main activity hosting the Compose UI.
 * Handles runtime permission requests for storage access and notifications.
 */
class FileShareActivity : ComponentActivity() {

    private val viewModel: FileShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installCrashHandler()
        enableEdgeToEdge()
        handleShareIntent(intent)

        setContent {
            val settings by viewModel.appSettings.collectAsState()
            val settingsLoaded by viewModel.settingsLoaded.collectAsState()
            val systemIsDark = isSystemInDarkTheme()

            val useDarkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemIsDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Track whether permissions have been requested this session
            var permissionsRequested by androidx.compose.runtime.mutableStateOf(hasStoragePermission())

            LocalShareTheme(
                darkTheme = useDarkTheme,
                colorPalette = settings.colorPalette,
                amoledMode = settings.amoledMode,
                themeColorSeed = settings.themeColorSeed
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!settingsLoaded) {
                        // Show nothing while settings are loading
                    } else {
                        when {
                            // First launch: show onboarding
                            !settings.onboardingCompleted -> {
                                OnboardingScreen(
                                    onComplete = { viewModel.completeOnboarding() }
                                )
                            }
                            // Permissions not yet granted: show permission screen
                            !permissionsRequested && !hasStoragePermission() -> {
                                PermissionsScreen(
                                    onPermissionsGranted = { permissionsRequested = true },
                                    onSkip = { permissionsRequested = true }
                                )
                            }
                            // Normal app flow
                            else -> {
                                LocalShareApp(viewModel = viewModel)

                                // Show incoming transfer dialog when another device pushes files
                                val incomingTransfer by viewModel.incomingTransfer.collectAsState()
                                incomingTransfer?.let { session ->
                                    if (session.status == com.localshare.app.data.SessionStatus.PENDING) {
                                        IncomingTransferDialog(
                                            session = session,
                                            onAccept = { viewModel.acceptTransfer(it) },
                                            onReject = { viewModel.rejectTransfer(it) },
                                            onDismiss = { viewModel.dismissIncomingTransfer() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Pre-request install permission so updates work smoothly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                // Don't force it, but let the user know when they try to update
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) ==
                    PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun handleShareIntent(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_SEND) {
            val uri = intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
            if (uri != null) {
                val fileRepo = com.localshare.app.data.FileRepository(applicationContext)
                val files = fileRepo.resolveUris(listOf(uri))
                viewModel.addSharedFiles(files)
            }
        } else if (intent?.action == android.content.Intent.ACTION_SEND_MULTIPLE) {
            val uris = intent.getParcelableArrayListExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
            if (uris != null && uris.isNotEmpty()) {
                val fileRepo = com.localshare.app.data.FileRepository(applicationContext)
                val files = fileRepo.resolveUris(uris)
                viewModel.addSharedFiles(files)
            }
        }
    }

    private fun installCrashHandler() {
        val crashRepo = CrashRepository(applicationContext)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val stackTrace = throwable.stackTraceToString()
                val report = CrashReport(
                    timestamp = System.currentTimeMillis(),
                    exceptionClass = throwable.javaClass.name,
                    message = throwable.message ?: "No message",
                    stackTrace = stackTrace,
                    deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                    androidVersion = "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
                )
                crashRepo.save(report)
            } catch (_: Exception) { }

            // Show a toast on the main thread before dying
            try {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(applicationContext, "LocalShare crashed. Report saved.", Toast.LENGTH_LONG).show()
                }
                Thread.sleep(2000)
            } catch (_: Exception) { }

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
