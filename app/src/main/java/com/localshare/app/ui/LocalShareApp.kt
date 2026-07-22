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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import com.localshare.app.ui.screens.HomeScreen
import com.localshare.app.ui.screens.SettingsScreen
import com.localshare.app.ui.screens.AboutScreen
import com.localshare.app.ui.screens.LogsScreen
import com.localshare.app.ui.screens.HowToUseScreen
import com.localshare.app.ui.screens.BugReportScreen
import com.localshare.app.ui.screens.PrivacyPolicyScreen
import androidx.compose.runtime.collectAsState
import com.localshare.app.data.ThemeMode
import androidx.compose.ui.layout.ContentScale
import com.localshare.app.ui.utils.bounceScale
import androidx.compose.foundation.layout.height

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
    data object Send : Screen("send", "Send", Icons.Filled.Home, Icons.Outlined.Home)
    data object SharedFiles : Screen("shared_files", "Shared", Icons.Rounded.History, Icons.Rounded.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val screens = listOf(Screen.Send, Screen.SharedFiles, Screen.Settings)

// ─── Main App Composable ───────────────────────────────────────────

@Composable
fun AnyShareApp(viewModel: FileShareViewModel = viewModel()) {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = "main_flow",
        enterTransition = { 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(400))
        },
        exitTransition = { 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeOut(tween(400))
        },
        popEnterTransition = { 
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(tween(400))
        },
        popExitTransition = { 
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeOut(tween(400))
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
            com.localshare.app.ui.screens.SharedFilesScreen(
                onNavigateBack = { rootNavController.popBackStack() },
                viewModel = viewModel
            )
        }
        composable("logs") {
            LogsScreen(
                viewModel = viewModel,
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("how_to_use") {
            HowToUseScreen(
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("bug_report") {
            BugReportScreen(
                onBack = { rootNavController.popBackStack() }
            )
        }
        composable("privacy_policy") {
            PrivacyPolicyScreen(
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
                                text = if (currentScreen == Screen.Send) "AnyShare" else currentScreen.title,
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
                enterTransition = { 
                    fadeIn(animationSpec = tween(300, easing = LinearEasing)) + 
                    scaleIn(initialScale = 0.92f, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
                },
                exitTransition = { 
                    fadeOut(animationSpec = tween(300, easing = LinearEasing)) + 
                    scaleOut(targetScale = 0.92f, animationSpec = tween(300, easing = FastOutSlowInEasing)) 
                }
            ) {
                composable(Screen.Send.route) {
                    com.localshare.app.ui.screens.SendScreen(
                        viewModel = viewModel,
                        navController = rootNavController,
                        onFilesSelected = {
                            bottomNavController.navigate(Screen.SharedFiles.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.SharedFiles.route) {
                    com.localshare.app.ui.screens.SharedFilesScreen(
                        viewModel = viewModel,
                        onNavigateBack = { /* No-op, it's a tab now */ }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { rootNavController.navigate("about") },
                        onNavigateToHowToUse = { rootNavController.navigate("how_to_use") },
                        onNavigateToBugReport = { rootNavController.navigate("bug_report") },
                        onNavigateToPrivacyPolicy = { rootNavController.navigate("privacy_policy") },
                        onSubPageChanged = { settingsSubPageActive = it }
                    )
                }
            }
        }
    }
}
