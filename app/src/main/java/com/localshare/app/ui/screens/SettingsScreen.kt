package com.localshare.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.localshare.app.R
import com.localshare.app.data.ColorPalette
import com.localshare.app.data.ThemeMode
import com.localshare.app.ui.FileShareViewModel
import com.localshare.app.ui.utils.bounceClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.ui.graphics.Brush

// ─── Settings Category Data ─────────────────────────────────────────

private data class SettingsCategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgColor: @Composable () -> Color,
    val iconColor: @Composable () -> Color
)

@Composable
fun SettingsScreen(viewModel: FileShareViewModel, onNavigateToAbout: () -> Unit) {
    val settings by viewModel.appSettings.collectAsState()

    val systemIsDark = isSystemInDarkTheme()
    val useDarkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemIsDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    var showHowToUseDialog by remember { mutableStateOf(false) }

    if (showHowToUseDialog) {
        AlertDialog(
            onDismissRequest = { showHowToUseDialog = false },
            title = {
                Text(
                    text = "How to Use LocalShare",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("1. Connect to Wi-Fi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Make sure both this device and the receiving device are connected to the same Wi-Fi network or hotspot.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("2. Select Files", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Go to the Home or Files tab to pick the items you want to share.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("3. Start Server", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Ensure the server is running. You will see a green 'Server Running' indicator.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("4. Download on Receiver", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Open the web browser on the receiving device and type the URL shown on the Home tab, or use the Nearby tab if the receiver is using the LocalShare app.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHowToUseDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val contentAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "ContentAlpha"
    )

    val contentOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "ContentOffset"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // State for expanded settings sections
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

    BackHandler(enabled = selectedCategoryIndex != null) {
        selectedCategoryIndex = null
    }

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
        val categories = listOf(
            Triple("Appearance", "Theme, colors & visual style", Icons.Rounded.Brush),
            Triple("Security", "PIN protection & access", Icons.Rounded.Lock),
            Triple("Device", "Name, connections & identity", Icons.Rounded.Devices),
            Triple("About", "Version, developer & source", Icons.Rounded.Info),
            Triple("How to Use", "Simple guide on using this app", Icons.Rounded.Help)
        )

        androidx.compose.animation.AnimatedContent(
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
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
            // ─── Expressive Settings Group ──────────────────────────

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
                    onClick = {
                        if (index == 3) onNavigateToAbout()
                        else if (index == 4) showHowToUseDialog = true
                        else selectedCategoryIndex = index
                    },
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
                        // Icon Container
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.first)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = colors.second,
                                modifier = Modifier.size(24.dp)
                            )
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

                        // Chevron indicator for expandable items
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

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Footer ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = if (useDarkTheme) com.localshare.app.R.drawable.logo_dark else com.localshare.app.R.drawable.logo),
                contentDescription = "LocalShare",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LocalShare",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Developer: Kaif Azad",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
            } // End of Main List Column
        } // End of categoryIndex == null
        else { // Sub-screen
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar for sub-screen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = { selectedCategoryIndex = null },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = categories[categoryIndex].first,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                when (categoryIndex) {
                    0 -> { // Appearance Section
                        SettingsCard {
                            ThemeSetting(
                                selected = settings.themeMode,
                                onSelect = { viewModel.setThemeMode(it) }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            ColorSetting(
                                selected = settings.colorPalette,
                                onSelect = { viewModel.setColorPalette(it) }
                            )
                        }
                    }
                    1 -> { // Security Section
                        SettingsCard {
                            PinSetting(
                                currentPin = settings.pin,
                                onPinChange = { viewModel.setPin(it) }
                            )
                        }
                    }
                    2 -> { // Device Section
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
                        }
                    }
                }
            } // End of Sub-screen Column
        }
    } // End of AnimatedContent
    } // End of Main Scrollable Column
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
            Color(0xFF7D5260) to Color(0xFFFFD8E4), // Appearance — Pink
            Color(0xFF633B48) to Color(0xFFFFD8EC), // Security — Rose
            Color(0xFF004A77) to Color(0xFFC2E7FF), // Device — Blue
            Color(0xFF3F474D) to Color(0xFFDEE3EB), // About — Grey
            Color(0xFF386A20) to Color(0xFFB7F397)  // How to Use — Green
        )
    } else {
        listOf(
            Color(0xFFFFD8E4) to Color(0xFF631835), // Appearance — Pink
            Color(0xFFFFD8EC) to Color(0xFF631B4B), // Security — Rose
            Color(0xFFD7E3FF) to Color(0xFF005AC1), // Device — Blue
            Color(0xFFEFF1F7) to Color(0xFF44474F), // About — Grey
            Color(0xFFB7F397) to Color(0xFF042100)  // How to Use — Green
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp
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
                                    Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF4285F4),
                                            Color(0xFFEA4335),
                                            Color(0xFFFBBC05),
                                            Color(0xFF34A853),
                                            Color(0xFF4285F4)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
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


// ─── PIN Setting ───────────────────────────────────────────────────

@Composable
private fun PinSetting(currentPin: String?, onPinChange: (String?) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val isPinEnabled = currentPin != null

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "PIN Protection",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isPinEnabled) "Web UI is protected" else "Anyone can access",
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
            Text(
                "Set 4-Digit PIN",
                fontWeight = FontWeight.Bold
            )
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
                        if (error) {
                            Text("Enter exactly 4 digits")
                        }
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
                if (pin.length == 4) {
                    onConfirm(pin)
                } else {
                    error = true
                }
            }) {
                Text("Set PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ─── Device Name Setting ───────────────────────────────────────────

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

// ─── Max Connections Setting ───────────────────────────────────────

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

        // Current value badge
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
