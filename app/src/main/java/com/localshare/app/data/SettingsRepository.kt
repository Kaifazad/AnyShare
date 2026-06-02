package com.localshare.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists [AppSettings] to SharedPreferences and provides a cute device name generator.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("localshare_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_PALETTE = "color_palette"
        private const val KEY_PIN = "pin"
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_MAX_CONNECTIONS = "max_connections"
        private const val KEY_CUSTOM_FOLDERS = "custom_folders"
        private const val KEY_CATEGORY_TOGGLES = "category_toggles"

        private val CUTE_ADJECTIVES = listOf(
            "Happy", "Fluffy", "Chill", "Turbo", "Cosmic",
            "Mystic", "Jolly", "Brave", "Sparkly", "Sneaky",
            "Gentle", "Swift", "Cozy", "Lucky", "Zippy",
            "Pixel", "Fuzzy", "Breezy", "Sunny", "Starry"
        )

        private val CUTE_ANIMALS = listOf(
            "Panda", "Fox", "Penguin", "Dolphin", "Koala",
            "Otter", "Bunny", "Owl", "Kitten", "Puppy",
            "Tiger", "Dragon", "Phoenix", "Falcon", "Bear",
            "Wolf", "Deer", "Parrot", "Turtle", "Hamster"
        )

        /**
         * Generate a random cute device name like "Fluffy Panda" or "Turbo Penguin".
         */
        fun generateCuteName(): String {
            val adjective = CUTE_ADJECTIVES.random()
            val animal = CUTE_ANIMALS.random()
            return "$adjective $animal"
        }
    }

    /**
     * Load settings from SharedPreferences. Auto-generates device name on first run.
     */
    fun load(): AppSettings {
        val themeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val paletteName = prefs.getString(KEY_COLOR_PALETTE, ColorPalette.SYSTEM.name) ?: ColorPalette.SYSTEM.name
        val pin = prefs.getString(KEY_PIN, null)
        val deviceName = prefs.getString(KEY_DEVICE_NAME, null) ?: generateCuteName().also {
            // Save the generated name immediately so it persists
            prefs.edit().putString(KEY_DEVICE_NAME, it).apply()
        }
        val maxConnections = prefs.getInt(KEY_MAX_CONNECTIONS, 3)

        return AppSettings(
            themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.SYSTEM },
            colorPalette = try { ColorPalette.valueOf(paletteName) } catch (_: Exception) { ColorPalette.SYSTEM },
            pin = pin,
            deviceName = deviceName,
            maxConnections = maxConnections.coerceIn(1, 5)
        )
    }

    /**
     * Save all settings to SharedPreferences.
     */
    fun save(settings: AppSettings) {
        prefs.edit().apply {
            putString(KEY_THEME_MODE, settings.themeMode.name)
            putString(KEY_COLOR_PALETTE, settings.colorPalette.name)
            if (settings.pin != null) {
                putString(KEY_PIN, settings.pin)
            } else {
                remove(KEY_PIN)
            }
            putString(KEY_DEVICE_NAME, settings.deviceName)
            putInt(KEY_MAX_CONNECTIONS, settings.maxConnections)
            apply()
        }
    }

    /**
     * Load share config from SharedPreferences.
     */
    fun loadShareConfig(): ShareConfig {
        val customFolders = prefs.getStringSet(KEY_CUSTOM_FOLDERS, emptySet()) ?: emptySet()
        val togglesSet = prefs.getStringSet(KEY_CATEGORY_TOGGLES, emptySet()) ?: emptySet()
        
        val categoryToggles = FileCategory.entries.associateWith { category ->
            togglesSet.contains(category.name)
        }

        return ShareConfig(
            categoryToggles = categoryToggles,
            customFolderUris = customFolders
        )
    }

    /**
     * Save share config to SharedPreferences.
     */
    fun saveShareConfig(config: ShareConfig) {
        val togglesSet = config.categoryToggles
            .filter { it.value }
            .map { it.key.name }
            .toSet()

        prefs.edit().apply {
            putStringSet(KEY_CUSTOM_FOLDERS, config.customFolderUris)
            putStringSet(KEY_CATEGORY_TOGGLES, togglesSet)
            apply()
        }
    }
}
