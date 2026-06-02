package com.localshare.app.data

/**
 * Theme mode options.
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Color palette options for the app theme.
 */
enum class ColorPalette(val displayName: String, val previewColor: Long) {
    SYSTEM("System", 0xFF4285F4),       // Material You dynamic color (neutral representative)
    LOCALSHARE("LocalShare", 0xFF2C2C2C), // Current minimalist grey
    OCEAN("Ocean Blue", 0xFF1976D2),
    EMERALD("Emerald", 0xFF2E7D32),
    SUNSET("Sunset", 0xFFE65100),
    ROSE("Rose", 0xFFC2185B)
}

/**
 * Holds all persistent app settings.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.SYSTEM,
    val pin: String? = null,          // null = disabled, "1234" = active
    val deviceName: String = "",      // Auto-generated cute name
    val maxConnections: Int = 3       // 1..5 simultaneous browser connections
)
