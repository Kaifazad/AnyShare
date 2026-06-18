package com.localshare.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.localshare.app.R
import com.localshare.app.data.ColorPalette

// Google Sans Flex variable font with rounded axis for Google Sans Rounded-like appearance.
private const val GoogleSansFlexRond = 100f

@OptIn(ExperimentalTextApi::class)
val GoogleSansRounded = FontFamily(
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Light.weight),
            FontVariation.Setting("ROND", GoogleSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            FontVariation.Setting("ROND", GoogleSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
            FontVariation.Setting("ROND", GoogleSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
            FontVariation.Setting("ROND", GoogleSansFlexRond)
        )
    ),
    androidx.compose.ui.text.font.Font(
        resId = R.font.gflex_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
            FontVariation.Setting("ROND", GoogleSansFlexRond)
        )
    ),
)

// ─── Custom Color Palette ─────────────────────────────────────────

// Minimalist Dark Theme (LocalShare default)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0A0A0A)
val DarkSurfaceVariant = Color(0xFF141414)
val DarkOnSurface = Color(0xFFE0E0E0)
val DarkOnSurfaceVariant = Color(0xFFA0A0A0)

// Minimalist Light Theme (LocalShare default)
val LightBg = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightSurfaceVariant = Color(0xFFEBEBEB)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnSurfaceVariant = Color(0xFF5E5E5E)

// Accents
val AccentPrimaryDark = Color(0xFFD4D4D4)  // Soft white/grey
val AccentPrimaryLight = Color(0xFF2C2C2C) // Soft black

val ErrorRed = Color(0xFFCF6679)

// ─── Color Schemes: LocalShare (Original Minimalist) ──────────────

private val LocalShareDarkScheme = darkColorScheme(
    primary = AccentPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkOnSurface,
    secondary = AccentPrimaryDark,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkOnSurface,
    tertiary = AccentPrimaryDark,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceVariant,
    surfaceContainerHigh = Color(0xFF1E1E1E),
    error = ErrorRed,
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF1F1F1F)
)

private val LocalShareLightScheme = lightColorScheme(
    primary = AccentPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightOnSurface,
    secondary = AccentPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightOnSurface,
    tertiary = AccentPrimaryLight,
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceVariant,
    surfaceContainerHigh = Color(0xFFE0E0E0),
    error = ErrorRed,
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0)
)

// ─── Color Schemes: Ocean Blue ────────────────────────────────────

private val OceanDarkScheme = darkColorScheme(
    primary = Color(0xFF82B1FF),
    onPrimary = Color(0xFF003060),
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFF80DEEA),
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF004D4D),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFF0A0E14),
    surface = Color(0xFF0F1318),
    surfaceVariant = Color(0xFF162028),
    onBackground = Color(0xFFDDE3EA),
    onSurface = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF8FA4B8),
    surfaceContainer = Color(0xFF162028),
    surfaceContainerHigh = Color(0xFF1E2C38),
    error = ErrorRed,
    outline = Color(0xFF2C3E50),
    outlineVariant = Color(0xFF1A2836)
)

private val OceanLightScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF001F24),
    tertiary = Color(0xFF2E7D32),
    background = Color(0xFFF5F8FC),
    surface = Color(0xFFEDF2F7),
    surfaceVariant = Color(0xFFE1E8F0),
    onBackground = Color(0xFF0D1B2A),
    onSurface = Color(0xFF0D1B2A),
    onSurfaceVariant = Color(0xFF44566C),
    surfaceContainer = Color(0xFFE1E8F0),
    surfaceContainerHigh = Color(0xFFD0DCE6),
    error = ErrorRed,
    outline = Color(0xFFB0BEC5),
    outlineVariant = Color(0xFFCFD8DC)
)

// ─── Color Schemes: Emerald Green ─────────────────────────────────

private val EmeraldDarkScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003A02),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF0D3311),
    secondaryContainer = Color(0xFF1B5E20),
    onSecondaryContainer = Color(0xFFC8E6C9),
    tertiary = Color(0xFF80CBC4),
    background = Color(0xFF080E09),
    surface = Color(0xFF0C130D),
    surfaceVariant = Color(0xFF142016),
    onBackground = Color(0xFFDBE5DC),
    onSurface = Color(0xFFDBE5DC),
    onSurfaceVariant = Color(0xFF8EA893),
    surfaceContainer = Color(0xFF142016),
    surfaceContainerHigh = Color(0xFF1C2C1E),
    error = ErrorRed,
    outline = Color(0xFF2E4830),
    outlineVariant = Color(0xFF1A3020)
)

private val EmeraldLightScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF002204),
    secondary = Color(0xFF388E3C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = Color(0xFF002204),
    tertiary = Color(0xFF00796B),
    background = Color(0xFFF5FAF5),
    surface = Color(0xFFEDF5ED),
    surfaceVariant = Color(0xFFDFECDF),
    onBackground = Color(0xFF0D1B0F),
    onSurface = Color(0xFF0D1B0F),
    onSurfaceVariant = Color(0xFF3E5B42),
    surfaceContainer = Color(0xFFDFECDF),
    surfaceContainerHigh = Color(0xFFCEE0CE),
    error = ErrorRed,
    outline = Color(0xFFA5C5A8),
    outlineVariant = Color(0xFFC1D9C3)
)

// ─── Color Schemes: Sunset Orange ─────────────────────────────────

private val SunsetDarkScheme = darkColorScheme(
    primary = Color(0xFFFFAB91),
    onPrimary = Color(0xFF4A1500),
    primaryContainer = Color(0xFFBF360C),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = Color(0xFFFFCC80),
    onSecondary = Color(0xFF3E2000),
    secondaryContainer = Color(0xFF5C3300),
    onSecondaryContainer = Color(0xFFFFE0B2),
    tertiary = Color(0xFFEF9A9A),
    background = Color(0xFF100A08),
    surface = Color(0xFF150E0B),
    surfaceVariant = Color(0xFF221812),
    onBackground = Color(0xFFE8DDD8),
    onSurface = Color(0xFFE8DDD8),
    onSurfaceVariant = Color(0xFFB09888),
    surfaceContainer = Color(0xFF221812),
    surfaceContainerHigh = Color(0xFF30221A),
    error = ErrorRed,
    outline = Color(0xFF4A3328),
    outlineVariant = Color(0xFF33221A)
)

private val SunsetLightScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCE),
    onPrimaryContainer = Color(0xFF2C0700),
    secondary = Color(0xFFE68A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = Color(0xFF261800),
    tertiary = Color(0xFFD32F2F),
    background = Color(0xFFFFF8F5),
    surface = Color(0xFFFFF0EB),
    surfaceVariant = Color(0xFFFEE5DB),
    onBackground = Color(0xFF2C1407),
    onSurface = Color(0xFF2C1407),
    onSurfaceVariant = Color(0xFF6B4A39),
    surfaceContainer = Color(0xFFFEE5DB),
    surfaceContainerHigh = Color(0xFFFCD3C2),
    error = ErrorRed,
    outline = Color(0xFFCFAF9F),
    outlineVariant = Color(0xFFE2C7B8)
)

// ─── Color Schemes: Rose Pink ─────────────────────────────────────

private val RoseDarkScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color(0xFF4A0028),
    primaryContainer = Color(0xFF880E4F),
    onPrimaryContainer = Color(0xFFFCE4EC),
    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF3A0040),
    secondaryContainer = Color(0xFF4A148C),
    onSecondaryContainer = Color(0xFFF3E5F5),
    tertiary = Color(0xFFEF9A9A),
    background = Color(0xFF100810),
    surface = Color(0xFF150C15),
    surfaceVariant = Color(0xFF22142A),
    onBackground = Color(0xFFE8DDE8),
    onSurface = Color(0xFFE8DDE8),
    onSurfaceVariant = Color(0xFFB098B0),
    surfaceContainer = Color(0xFF22142A),
    surfaceContainerHigh = Color(0xFF301C3A),
    error = ErrorRed,
    outline = Color(0xFF4A2848),
    outlineVariant = Color(0xFF331A33)
)

private val RoseLightScheme = lightColorScheme(
    primary = Color(0xFFC2185B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCE4EC),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF7B1FA2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF270040),
    tertiary = Color(0xFFD32F2F),
    background = Color(0xFFFFF5F8),
    surface = Color(0xFFFFF0F3),
    surfaceVariant = Color(0xFFFCE4EC),
    onBackground = Color(0xFF2C0D1A),
    onSurface = Color(0xFF2C0D1A),
    onSurfaceVariant = Color(0xFF6B3954),
    surfaceContainer = Color(0xFFFFD8E6),
    surfaceContainerHigh = Color(0xFFFFC2D8),
    error = ErrorRed,
    outline = Color(0xFFCF99B5),
    outlineVariant = Color(0xFFE2B8CC)
)

// ─── Palette Selection Helper ─────────────────────────────────────

/**
 * Returns the appropriate dark/light ColorScheme for a given [ColorPalette].
 */
fun getColorScheme(palette: ColorPalette, darkTheme: Boolean): androidx.compose.material3.ColorScheme? {
    return when (palette) {
        ColorPalette.SYSTEM -> null // Caller should use dynamic color
        ColorPalette.LOCALSHARE -> if (darkTheme) LocalShareDarkScheme else LocalShareLightScheme
        ColorPalette.OCEAN -> if (darkTheme) OceanDarkScheme else OceanLightScheme
        ColorPalette.EMERALD -> if (darkTheme) EmeraldDarkScheme else EmeraldLightScheme
        ColorPalette.SUNSET -> if (darkTheme) SunsetDarkScheme else SunsetLightScheme
        ColorPalette.ROSE -> if (darkTheme) RoseDarkScheme else RoseLightScheme
    }
}

// ─── Typography (Outfit — Rounded, Modern) ────────────────────────

val LocalShareTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSansRounded,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ─── M3 Expressive Shapes ─────────────────────────────────────────

val LocalShareShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// ─── Theme Composable ─────────────────────────────────────────────

@Composable
fun LocalShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPalette: ColorPalette = ColorPalette.SYSTEM,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        colorPalette == ColorPalette.SYSTEM && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorScheme(
            if (colorPalette == ColorPalette.SYSTEM) ColorPalette.LOCALSHARE else colorPalette,
            darkTheme
        ) ?: (if (darkTheme) LocalShareDarkScheme else LocalShareLightScheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LocalShareTypography,
        shapes = LocalShareShapes,
        content = content
    )
}
