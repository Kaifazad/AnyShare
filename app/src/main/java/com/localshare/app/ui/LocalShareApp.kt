package com.localshare.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import com.localshare.app.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.localshare.app.ui.screens.FilesScreen
import com.localshare.app.ui.screens.FilePreviewScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import com.localshare.app.ui.screens.HomeScreen
import com.localshare.app.ui.screens.SettingsScreen
import com.localshare.app.ui.screens.AboutScreen
import com.localshare.app.ui.screens.LogsScreen
import androidx.compose.runtime.collectAsState
import com.localshare.app.data.ThemeMode
import androidx.compose.ui.layout.ContentScale
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.foundation.layout.height

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavController

// ─── Navigation Routes ─────────────────────────────────────────────
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import com.localshare.app.util.FormatUtils

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Send : Screen("send", "Send", Icons.Filled.Home, Icons.Outlined.Home) // Using Home icon for Send temporarily, will change later
    data object Receive : Screen("receive", "Receive", Icons.Filled.WifiTethering, Icons.Outlined.WifiTethering)
    data object History : Screen("history", "History", Icons.Rounded.History, Icons.Rounded.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val screens = listOf(Screen.Send, Screen.Receive, Screen.History, Screen.Settings)

// ─── Main App Composable ───────────────────────────────────────────

@Composable
fun LocalShareApp(viewModel: FileShareViewModel = viewModel()) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "main_flow",
        enterTransition = { 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) 
        },
        exitTransition = { 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) 
        },
        popEnterTransition = { 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) 
        },
        popExitTransition = { 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) 
        }
    ) {
        composable("main_flow") {
            MainFlow(viewModel = viewModel, rootNavController = rootNavController)
        }
        composable("about") {
            AboutScreen(
                onBackClick = { rootNavController.popBackStack() }
            )
        }
        composable("transfer_history") {
            com.localshare.app.ui.screens.TransferHistoryScreen(
                onNavigateBack = { rootNavController.popBackStack() }
            )
        }
        composable("logs") {
            LogsScreen(
                viewModel = viewModel,
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("file_preview/{fileId}") { backStackEntry ->
            val fileId = backStackEntry.arguments?.getString("fileId")?.toLongOrNull()
            val file = fileId?.let { viewModel.getFileById(it) }
            FilePreviewScreen(
                file = file,
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("uri_preview?uri={uri}&name={name}&type={type}") { backStackEntry ->
            val uriStr = backStackEntry.arguments?.getString("uri")
            val name = backStackEntry.arguments?.getString("name") ?: "File"
            val type = backStackEntry.arguments?.getString("type") ?: "*/*"
            
            val dummyFile = uriStr?.let {
                com.localshare.app.data.SharedFile(
                    id = 0,
                    name = name,
                    path = "",
                    uri = android.net.Uri.parse(android.net.Uri.decode(it)),
                    size = 0,
                    mimeType = type,
                    category = com.localshare.app.data.FileCategory.DOCUMENTS,
                    lastModified = 0
                )
            }
            FilePreviewScreen(
                file = dummyFile,
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("history_details/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            if (sessionId != null) {
                com.localshare.app.ui.screens.TransferHistoryDetailsScreen(
                    sessionId = sessionId,
                    onBack = { rootNavController.popBackStack() },
                    navController = rootNavController
                )
            } else {
                rootNavController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFlow(viewModel: FileShareViewModel, rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentScreen = screens.find { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    } ?: Screen.Send

    val settings by viewModel.appSettings.collectAsState()


    var settingsSubPageActive by remember { mutableStateOf(false) }

    val incomingSession by viewModel.incomingTransfer.collectAsState()
    
    val updateInfo by viewModel.updateInfo.collectAsState()
    var hasShownUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        val currentVersion = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
        viewModel.checkForUpdates(currentVersion)
    }

    if (updateInfo != null && !hasShownUpdateDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { hasShownUpdateDialog = true },
            title = { Text("Update Available") },
            text = { Text("A new version (${updateInfo?.version}) of LocalShare is available! Would you like to download it now?") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    hasShownUpdateDialog = true
                    if (updateInfo?.apkUrl != null) {
                        com.localshare.app.util.UpdateManager.downloadAndInstallUpdate(context, updateInfo!!.apkUrl!!)
                    } else {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(updateInfo?.releaseUrl))
                        context.startActivity(intent)
                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { hasShownUpdateDialog = true }) {
                    Text("Later")
                }
            }
        )
    }

    // Bottom sheet style accept/reject (slides up from bottom)
    if (incomingSession != null && incomingSession?.status == com.localshare.app.data.SessionStatus.PENDING) {
        com.localshare.app.ui.screens.IncomingTransferDialog(
            session = incomingSession!!,
            onAccept = { viewModel.acceptTransfer(it) },
            onReject = { viewModel.rejectTransfer(it) },
            onDismiss = { viewModel.dismissIncomingTransfer() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!settingsSubPageActive) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (currentScreen == Screen.Send) "LocalShare" else currentScreen.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    actions = {
                        // History is now a tab, no need for top bar action
                    }
                )
            }
        },
        bottomBar = {
            if (!settingsSubPageActive) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp
                ) {
                    screens.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (screen.route == "settings" && updateInfo != null) {
                                    androidx.compose.material3.BadgedBox(
                                        badge = {
                                            androidx.compose.material3.Badge(
                                                containerColor = androidx.compose.ui.graphics.Color.Red
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title, 
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // When settings sub-page is active, don't apply the outer padding
        // (the sub-page has its own Scaffold with its own padding)
        val effectivePadding = if (settingsSubPageActive) PaddingValues(0.dp) else innerPadding
        Box(modifier = Modifier.fillMaxSize().padding(effectivePadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Screen.Send.route,
                enterTransition = { fadeIn(animationSpec = tween(200)) },
                exitTransition = { fadeOut(animationSpec = tween(200)) }
            ) {
                composable(Screen.Send.route) {
                    com.localshare.app.ui.screens.SendScreen(
                        viewModel = viewModel,
                        navController = rootNavController
                    )
                }
                composable(Screen.Receive.route) {
                    com.localshare.app.ui.screens.ReceiveScreenTab(viewModel = viewModel)
                }
                composable(Screen.History.route) {
                    com.localshare.app.ui.screens.TransferHistoryScreen(
                        onNavigateBack = { /* No-op, it's a tab now */ }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { rootNavController.navigate("about") },
                        onSubPageChanged = { settingsSubPageActive = it }
                    )
                }
            }
        }
    }
}
