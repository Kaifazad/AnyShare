package com.localshare.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import com.localshare.app.data.ThemeMode
import com.localshare.app.data.ColorPalette
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.theme.colorSchemeFromSeed
import com.localshare.app.ui.theme.neutralColorScheme
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import com.localshare.app.ui.utils.bounceClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SystemUpdate

// ─── Main Settings Screen ──────────────────────────────────────────

@Composable
fun SettingsScreen(
    viewModel: FileShareViewModel,
    onNavigateToAbout: () -> Unit,
    onNavigateToHowToUse: () -> Unit,
    onNavigateToBugReport: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onSubPageChanged: ((Boolean) -> Unit)? = null
) {
    val settings by viewModel.appSettings.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    val updateInfo by viewModel.updateInfo.collectAsState()

    val contentAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ContentAlpha"
    )
    val contentOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "ContentOffset"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

    BackHandler(enabled = selectedCategoryIndex != null) {
        selectedCategoryIndex = null
    }

    // Report sub-page state to parent
    LaunchedEffect(selectedCategoryIndex) {
        onSubPageChanged?.invoke(selectedCategoryIndex != null)
    }

    val categories = listOf(
        Triple("Appearance", "Theme, colors & visual style", Icons.Rounded.Brush),
        Triple("Device", "Name, connections & identity", Icons.Rounded.Devices),
        Triple("Security", "PIN protection & access", Icons.Rounded.Lock),
        Triple("Storage", "Folder size, images & videos", Icons.Rounded.Storage),
        Triple("How to Use", "Simple guide on using this app", Icons.AutoMirrored.Rounded.Help),
        Triple("Updates", "Release notes & bug fixes", Icons.Rounded.SystemUpdate),
        Triple("Report a Bug", "Report issues or request features", Icons.Rounded.BugReport),
        Triple("Privacy Policy", "How we handle your data", Icons.Rounded.Security),
        Triple("About", "Version, developer & source", Icons.Rounded.Info)
    )

    AnimatedContent(
        targetState = selectedCategoryIndex,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
            } else {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
            }
        },
        label = "SettingsNav"
    ) { categoryIndex ->
        if (categoryIndex == null) {
            // ─── Main Settings List ────────────────────────────
            MainSettingsList(
                categories = categories,
                isDark = isDark,
                contentAlpha = contentAlpha,
                contentOffset = contentOffset,
                updateInfo = updateInfo,
                onSelectCategory = { index ->
                    when (index) {
                        4 -> onNavigateToHowToUse()
                        6 -> onNavigateToBugReport()
                        7 -> onNavigateToPrivacyPolicy()
                        8 -> onNavigateToAbout()
                        else -> selectedCategoryIndex = index
                    }
                }
            )
        } else {
            // ─── Sub-page with Scaffold + LargeTopAppBar ───────
            SettingsSubPage(
                title = categories[categoryIndex].first,
                onBack = { selectedCategoryIndex = null }
            ) { paddingValues ->
                when (categoryIndex) {
                    0 -> AppearanceContent(settings, viewModel, paddingValues)
                    1 -> DeviceContent(settings, viewModel, paddingValues)
                    2 -> SecurityContent(settings, viewModel, paddingValues)
                    3 -> StorageContent(paddingValues)
                    5 -> UpdatesContent(viewModel, updateInfo, paddingValues)
                }
            }
        }
    }
}

// ─── Main Settings List ──────────────────────────────────────────

@Composable
private fun MainSettingsList(
    categories: List<Triple<String, String, ImageVector>>,
    isDark: Boolean,
    contentAlpha: Float,
    contentOffset: androidx.compose.ui.unit.Dp,
    updateInfo: com.localshare.app.util.UpdateInfo?,
    onSelectCategory: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffset.toPx()
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        val categoryColors = getCategoryColors(isDark)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Transparent)
        ) {
            categories.forEachIndexed { index, (title, subtitle, icon) ->
                val shape = shapeForIndex(index, categories.size)
                val colors = categoryColors[index]

                Surface(
                    onClick = { onSelectCategory(index) },
                    shape = shape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.first)
                        ) {
                            if (title == "Updates" && updateInfo != null) {
                                androidx.compose.material3.BadgedBox(
                                    badge = {
                                        androidx.compose.material3.Badge(
                                            containerColor = Color.Red,
                                            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = colors.second,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = colors.second,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = subtitle,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (index < 3) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (index < categories.size - 1) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Settings Sub-page Scaffold (like About page) ────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSubPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

// ─── Appearance Content ──────────────────────────────────────────

@Composable
private fun AppearanceContent(settings: com.localshare.app.data.AppSettings, viewModel: FileShareViewModel, paddingValues: PaddingValues) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Wallpaper dynamic colors (Android 12+)
    val wallpaperScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else null

    // Extract wallpaper seed colors
    val wallpaperSeeds = remember(wallpaperScheme) {
        if (wallpaperScheme != null) {
            listOf(
                "Primary" to wallpaperScheme.primary,
                "Secondary" to wallpaperScheme.secondary,
                "Tertiary" to wallpaperScheme.tertiary,
            ).distinctBy { it.second }
        } else emptyList()
    }

    // Preset seed colors
    val presetSeeds = listOf(
        "Blue" to 0xFF1565C0.toInt(),
        "Teal" to 0xFF00695C.toInt(),
        "Green" to 0xFF2E7D32.toInt(),
        "Purple" to 0xFF6A1B9A.toInt(),
        "Pink" to 0xFFAD1457.toInt(),
        "Orange" to 0xFFE65100.toInt(),
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val currentSeed = settings.themeColorSeed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ─── Color Section ────────────────────────────────────
        Text(
            text = "Color",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Icons, text, and more match colors in your wallpaper",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Color circles
        if (selectedTab == 0) {
            // Wallpaper colors tab
            if (wallpaperSeeds.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // System option (full dynamic scheme)
                    item {
                        val isSelected = currentSeed == "system"
                        SeedColorCircle(
                            seedArgb = null,
                            label = "System",
                            isSelected = isSelected,
                            scheme = wallpaperScheme,
                            onClick = { viewModel.setThemeColorSeed("system") }
                        )
                    }
                    // Wallpaper-derived seeds
                    items(wallpaperSeeds.size) { index ->
                        val (name, color) = wallpaperSeeds[index]
                        val hex = String.format("%08X", color.toArgb())
                        val isSelected = currentSeed == hex
                        SeedColorCircle(
                            seedArgb = color.toArgb(),
                            label = name,
                            isSelected = isSelected,
                            onClick = { viewModel.setThemeColorSeed(hex) }
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        text = "Dynamic colors require Android 12 or later",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // Other colors tab — neutral + presets
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Neutral option
                item {
                    val isSelected = currentSeed == "neutral"
                    SeedColorCircle(
                        seedArgb = 0xFF247EE0.toInt(),
                        label = "Neutral",
                        isSelected = isSelected,
                        onClick = { viewModel.setThemeColorSeed("neutral") }
                    )
                }
                // Preset seeds
                items(presetSeeds.size) { index ->
                    val (name, argb) = presetSeeds[index]
                    val hex = String.format("%08X", argb)
                    val isSelected = currentSeed == hex
                    SeedColorCircle(
                        seedArgb = argb,
                        label = name,
                        isSelected = isSelected,
                        onClick = { viewModel.setThemeColorSeed(hex) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pill tabs
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PillTab(selected = selectedTab == 0, text = "Wallpaper colors", modifier = Modifier.weight(1f)) { selectedTab = 0 }
                PillTab(selected = selectedTab == 1, text = "Other colors", modifier = Modifier.weight(1f)) { selectedTab = 1 }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ─── Theme Toggle Cards ──────────────────────────────
        ThemeToggleCard(
            title = "Follow System Theme",
            description = "When enabled, the app follows your device's light/dark mode setting automatically. Disable to manually choose light or dark mode below.",
            checked = settings.themeMode == ThemeMode.SYSTEM,
            onCheckedChange = { viewModel.setThemeMode(if (it) ThemeMode.SYSTEM else ThemeMode.LIGHT) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemeToggleCard(
            title = "Use Dark Mode",
            description = "Forces the app to use dark mode regardless of your system setting. Only available when \"Follow System Theme\" is disabled.",
            checked = settings.themeMode == ThemeMode.DARK,
            enabled = settings.themeMode != ThemeMode.SYSTEM,
            onCheckedChange = { viewModel.setThemeMode(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemeToggleCard(
            title = "Use Amoled Mode",
            description = "Pure black background for AMOLED screens. Saves battery and reduces eye strain in dark environments.",
            checked = settings.amoledMode,
            onCheckedChange = { viewModel.setAmoledMode(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ThemeToggleCard(
            title = "Haptic Feedback",
            description = "Vibrate on button taps for tactile feedback. Disable if you prefer silent interactions.",
            checked = settings.hapticEnabled,
            onCheckedChange = { viewModel.setHapticEnabled(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SeedColorCircle(
    seedArgb: Int?,
    label: String,
    isSelected: Boolean,
    scheme: androidx.compose.material3.ColorScheme? = null,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Generate color scheme from seed
    val generatedScheme = remember(seedArgb, isDark) {
        when {
            scheme != null -> scheme
            seedArgb != null -> colorSchemeFromSeed(seedArgb, isDark)
            else -> neutralColorScheme(isDark)
        }
    }

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        label = "borderWidth"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) generatedScheme.primary else Color.Transparent,
        label = "borderColor"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(borderWidth, borderColor, CircleShape)
                .then(if (borderWidth > 0.dp) Modifier.padding(borderWidth) else Modifier)
                .clickable(onClick = onClick)
        ) {
            // 4-quadrant color grid
            Column(modifier = Modifier.matchParentSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(generatedScheme.primary))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(generatedScheme.secondary))
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(generatedScheme.tertiary))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(generatedScheme.primaryContainer))
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PillTab(selected: Boolean, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "pillTabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "pillTabText"
    )
    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

@Composable
private fun ThemeToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = if (enabled) onCheckedChange else null,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ─── Security Content ────────────────────────────────────────────

@Composable
private fun SecurityContent(settings: com.localshare.app.data.AppSettings, viewModel: FileShareViewModel, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Security Status Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (settings.pin != null || settings.encryptionEnabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (settings.pin != null || settings.encryptionEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (settings.pin != null || settings.encryptionEnabled) "Protected" else "Unprotected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (settings.pin != null || settings.encryptionEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildString {
                            if (settings.pin != null) append("PIN: ON")
                            if (settings.pin != null && settings.encryptionEnabled) append("  |  ")
                            if (settings.encryptionEnabled) append("Encryption: ON")
                            if (isEmpty()) append("No security features enabled")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsCard {
            PinSetting(
                currentPin = settings.pin,
                onPinChange = { viewModel.setPin(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            EncryptionSetting(
                enabled = settings.encryptionEnabled,
                onToggle = { viewModel.setEncryptionEnabled(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            ClipboardSyncSetting(
                enabled = settings.clipboardSyncEnabled,
                onToggle = { viewModel.setClipboardSyncEnabled(it) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── Device Content ──────────────────────────────────────────────

@Composable
private fun DeviceContent(settings: com.localshare.app.data.AppSettings, viewModel: FileShareViewModel, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        SettingsCard {
            DeviceNameSetting(
                name = settings.deviceName,
                onNameChange = { viewModel.setDeviceName(it) },
                onRandomize = { viewModel.randomizeDeviceName() }
            )
            Spacer(modifier = Modifier.height(20.dp))
            MaxConnectionsSetting(
                maxConnections = settings.maxConnections,
                onMaxChange = { viewModel.setMaxConnections(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            NearbyDiscoverySetting(
                enabled = settings.enableNearbyDiscovery,
                onToggle = { viewModel.setEnableNearbyDiscovery(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            BatteryOptimizationSetting()
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
//  COMPOSABLE BUILDING BLOCKS
// ═══════════════════════════════════════════════════════════════════

private fun shapeForIndex(index: Int, total: Int): RoundedCornerShape {
    return when {
        total == 1 -> RoundedCornerShape(24.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 24.dp, topEnd = 24.dp,
            bottomStart = 4.dp, bottomEnd = 4.dp
        )
        index == total - 1 -> RoundedCornerShape(
            topStart = 4.dp, topEnd = 4.dp,
            bottomStart = 24.dp, bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(4.dp)
    }
}

@Composable
private fun getCategoryColors(isDark: Boolean): List<Pair<Color, Color>> {
    return if (isDark) {
        listOf(
            Color(0xFF7D5260) to Color(0xFFFFD8E4), // Appearance
            Color(0xFF004A77) to Color(0xFFC2E7FF), // Device
            Color(0xFF633B48) to Color(0xFFFFD8EC), // Security
            Color(0xFF3F474D) to Color(0xFFDEE3EB), // Storage
            Color(0xFF386A20) to Color(0xFFB7F397), // How to Use
            Color(0xFF4B3900) to Color(0xFFFFE082), // Updates
            Color(0xFF6B3A2A) to Color(0xFFFFCBA4), // Report a Bug
            Color(0xFF2A4A6B) to Color(0xFFA4D4FF), // Privacy Policy
            Color(0xFF3F474D) to Color(0xFFDEE3EB)  // About
        )
    } else {
        listOf(
            Color(0xFFFFD8E4) to Color(0xFF631835), // Appearance
            Color(0xFFD7E3FF) to Color(0xFF005AC1), // Device
            Color(0xFFFFD8EC) to Color(0xFF631B4B), // Security
            Color(0xFFEFF1F7) to Color(0xFF44474F), // Storage
            Color(0xFFB7F397) to Color(0xFF042100), // How to Use
            Color(0xFFFFE082) to Color(0xFF4B3900), // Updates
            Color(0xFFFFCBA4) to Color(0xFF6B3A2A), // Report a Bug
            Color(0xFFA4D4FF) to Color(0xFF2A4A6B), // Privacy Policy
            Color(0xFFEFF1F7) to Color(0xFF44474F)  // About
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ThemeSetting(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                Triple(ThemeMode.SYSTEM, "System", Icons.Rounded.PhoneAndroid),
                Triple(ThemeMode.LIGHT, "Light", Icons.Rounded.LightMode),
                Triple(ThemeMode.DARK, "Dark", Icons.Rounded.DarkMode)
            )
            modes.forEach { (mode, label, icon) ->
                val isSelected = selected == mode
                val bgColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                    label = "themeBg"
                )
                val contentColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "themeFg"
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onSelect(mode) },
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                             else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSetting(selected: ColorPalette, onSelect: (ColorPalette) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Accent Color",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ColorPalette.entries.forEach { palette ->
                val isSelected = palette == selected
                val borderColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    label = "colorBorder"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.bounceClick { onSelect(palette) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = borderColor,
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (palette == ColorPalette.SYSTEM) {
                                    androidx.compose.ui.graphics.Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF4285F4),
                                            Color(0xFFEA4335),
                                            Color(0xFFFBBC05),
                                            Color(0xFF34A853),
                                            Color(0xFF4285F4)
                                        )
                                    )
                                } else {
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(
                                            Color(palette.previewColor),
                                            Color(palette.previewColor).copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = palette.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PinSetting(currentPin: String?, onPinChange: (String?) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val isPinEnabled = currentPin != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PIN Protection",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPinEnabled) Color(0xFF22C55E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isPinEnabled) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPinEnabled) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = if (isPinEnabled) "Web UI is protected with PIN" else "Anyone on the network can access",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isPinEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    showDialog = true
                } else {
                    onPinChange(null)
                }
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }

    if (showDialog) {
        PinInputDialog(
            onConfirm = { pin ->
                onPinChange(pin)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun PinInputDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set 4-Digit PIN", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Browsers must enter this PIN to access your files.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { newValue ->
                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            pin = newValue
                            error = false
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (visible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                                contentDescription = "Toggle visibility"
                            )
                        }
                    },
                    isError = error,
                    supportingText = {
                        if (error) Text("Enter exactly 4 digits")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin.length == 4) onConfirm(pin) else error = true
            }) {
                Text("Set PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeviceNameSetting(
    name: String,
    onNameChange: (String) -> Unit,
    onRandomize: () -> Unit
) {
    SettingsLabel(text = "Device Name")
    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 24) onNameChange(it) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onRandomize,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Casino,
                contentDescription = "Random name",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Shown to connected devices",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun MaxConnectionsSetting(maxConnections: Int, onMaxChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Max Connections",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Up to $maxConnections device${if (maxConnections != 1) "s" else ""} at once",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$maxConnections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        value = maxConnections.toFloat(),
        onValueChange = { onMaxChange(it.toInt()) },
        valueRange = 1f..5f,
        steps = 3,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    )
}

@Composable
fun NearbyDiscoverySetting(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nearby Device Discovery",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Allow others on the network to easily find your device via Quick Share",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun EncryptionSetting(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "E2E Encryption",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (enabled) Color(0xFF22C55E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (enabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (enabled) "Files encrypted with AES-256-GCM" else "Encrypt file transfers end-to-end",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Info text
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = if (enabled) {
                    "Your files are encrypted before sending and decrypted on the other device. " +
                    "Even if someone intercepts the transfer, they cannot read your files. " +
                    "The encryption key is embedded in the URL automatically."
                } else {
                    "When enabled, every file transfer is encrypted with AES-256-GCM military-grade encryption. " +
                    "Recommended when sharing sensitive files on public or shared networks. " +
                    "No speed impact on home Wi-Fi."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun ClipboardSyncSetting(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val context = LocalContext.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Clipboard Sync",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (enabled) Color(0xFF22C55E).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (enabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (enabled) "Syncs clipboard to web UI" else "Share clipboard text between phone and laptop",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = enabled,
                onCheckedChange = {
                    onToggle(it)
                    val msg = if (it) "Clipboard sync enabled" else "Clipboard sync disabled"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Info text
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = if (enabled) {
                    "Your phone's clipboard text is shared with the web UI in real-time. " +
                    "Copy text on your phone and it appears on the laptop instantly, and vice versa. " +
                    "Note: Android may show a 'pasted from clipboard' notification."
                } else {
                    "When enabled, clipboard text is shared between your phone and any device " +
                    "connected to the web UI. Copy a link on your phone and paste it on your laptop instantly."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@android.annotation.SuppressLint("BatteryLife")
@Composable
private fun BatteryOptimizationSetting() {
    val context = LocalContext.current
    val pm = remember { context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager }
    var isIgnored by remember { mutableStateOf(pm.isIgnoringBatteryOptimizations(context.packageName)) }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isIgnored = pm.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Battery Optimization",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isIgnored) "Unrestricted background server" else "Disable for reliable background server",
                style = MaterialTheme.typography.bodySmall,
                color = if (isIgnored) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        if (isIgnored) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Optimizations Ignored",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(28.dp)
            )
        } else {
            FilledTonalButton(
                onClick = {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = android.net.Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to open battery settings", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Disable", style = MaterialTheme.typography.labelMedium)
        }
    }
}
}

// ─── Storage Content ───────────────────────────────────────────────

@Composable
fun StorageContent(paddingValues: PaddingValues) {
    var totalSize by remember { mutableStateOf(0L) }
    var imageCount by remember { mutableIntStateOf(0) }
    var videoCount by remember { mutableIntStateOf(0) }
    var otherCount by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val localShareDir = java.io.File(downloadsDir, "LocalShare")
            
            var size = 0L
            var images = 0
            var videos = 0
            var others = 0

            if (localShareDir.exists()) {
                localShareDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    size += file.length()
                    val ext = file.extension.lowercase()
                    when (ext) {
                        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic" -> images++
                        "mp4", "mkv", "avi", "mov", "webm" -> videos++
                        else -> others++
                    }
                }
            }
            
            totalSize = size
            imageCount = images
            videoCount = videos
            otherCount = others
            isLoading = false
        }
    }

    fun formatSize(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Total Space Used",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = formatSize(totalSize),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StorageStatItem(
                            icon = Icons.Rounded.Image,
                            label = "Images",
                            count = imageCount,
                            color = Color(0xFF4CAF50)
                        )
                        StorageStatItem(
                            icon = Icons.Rounded.Videocam,
                            label = "Videos",
                            count = videoCount,
                            color = Color(0xFFE91E63)
                        )
                        StorageStatItem(
                            icon = Icons.AutoMirrored.Rounded.InsertDriveFile,
                            label = "Other Files",
                            count = otherCount,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
        

    }
}

@Composable
private fun StorageStatItem(icon: ImageVector, label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UpdatesContent(
    viewModel: FileShareViewModel,
    updateInfo: com.localshare.app.util.UpdateInfo?,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val updateStatus by viewModel.updateStatus.collectAsState()

    val currentVersion = remember {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Icon(
            imageVector = Icons.Rounded.SystemUpdate,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "LocalShare",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Current Version: $currentVersion",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        when {
            updateStatus == FileShareViewModel.UpdateStatus.UPDATE_AVAILABLE && updateInfo != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("New Version Available: ${updateInfo.version}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = updateInfo.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (updateInfo.apkUrl != null) {
                                    com.localshare.app.util.UpdateManager.downloadAndInstallUpdate(context, updateInfo.apkUrl)
                                } else {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(updateInfo.releaseUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download & Install Update")
                        }
                    }
                }
            }
            updateStatus == FileShareViewModel.UpdateStatus.UP_TO_DATE -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("You're up to date!", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Version $currentVersion is the latest", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.checkForUpdates(currentVersion) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Check Again")
                        }
                    }
                }
            }
            updateStatus == FileShareViewModel.UpdateStatus.ERROR -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Check failed", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Could not reach GitHub. Check your internet connection.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.checkForUpdates(currentVersion) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Check for updates", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.checkForUpdates(currentVersion) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = updateStatus != FileShareViewModel.UpdateStatus.CHECKING
                        ) {
                            if (updateStatus == FileShareViewModel.UpdateStatus.CHECKING) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Checking...")
                            } else {
                                Text("Check for Updates")
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
