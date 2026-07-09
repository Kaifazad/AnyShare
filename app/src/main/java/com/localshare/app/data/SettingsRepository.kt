package com.localshare.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Persists [AppSettings] to DataStore and provides a cute device name generator.
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_COLOR_PALETTE = stringPreferencesKey("color_palette")
        private val KEY_THEME_COLOR_SEED = stringPreferencesKey("theme_color_seed")
        private val KEY_AMOLED_MODE = booleanPreferencesKey("amoled_mode")
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val KEY_PIN = stringPreferencesKey("pin")
        private val KEY_DEVICE_NAME = stringPreferencesKey("device_name")
        private val KEY_MAX_CONNECTIONS = intPreferencesKey("max_connections")
        private val KEY_ENABLE_NEARBY = booleanPreferencesKey("enable_nearby")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_ENCRYPTION_ENABLED = booleanPreferencesKey("encryption_enabled")

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

        fun generateCuteName(): String {
            val adjective = CUTE_ADJECTIVES.random()
            val animal = CUTE_ANIMALS.random()
            return "$adjective $animal"
        }
    }

    /**
     * Load settings from DataStore. Auto-generates device name on first run.
     */
    fun load(): AppSettings = runBlocking {
        val prefs = context.dataStore.data.first()

        val themeName = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        val paletteName = prefs[KEY_COLOR_PALETTE] ?: ColorPalette.SYSTEM.name
        val themeColorSeed = prefs[KEY_THEME_COLOR_SEED] ?: "system"
        val amoledMode = prefs[KEY_AMOLED_MODE] ?: false
        val hapticEnabled = prefs[KEY_HAPTIC_ENABLED] ?: true
        val pin = prefs[KEY_PIN]
        val deviceName = prefs[KEY_DEVICE_NAME] ?: generateCuteName().also { name ->
            context.dataStore.edit { it[KEY_DEVICE_NAME] = name }
        }
        val maxConnections = prefs[KEY_MAX_CONNECTIONS] ?: 3
        val enableNearby = prefs[KEY_ENABLE_NEARBY] ?: true
        val onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false
        val encryptionEnabled = prefs[KEY_ENCRYPTION_ENABLED] ?: false

        AppSettings(
            themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.SYSTEM },
            colorPalette = try { ColorPalette.valueOf(paletteName) } catch (_: Exception) { ColorPalette.SYSTEM },
            themeColorSeed = themeColorSeed,
            amoledMode = amoledMode,
            hapticEnabled = hapticEnabled,
            pin = pin,
            deviceName = deviceName,
            maxConnections = maxConnections.coerceIn(1, 5),
            enableNearbyDiscovery = enableNearby,
            onboardingCompleted = onboardingCompleted,
            encryptionEnabled = encryptionEnabled
        )
    }

    /**
     * Save all settings to DataStore.
     */
    fun save(settings: AppSettings) = runBlocking {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = settings.themeMode.name
            prefs[KEY_COLOR_PALETTE] = settings.colorPalette.name
            prefs[KEY_THEME_COLOR_SEED] = settings.themeColorSeed
            prefs[KEY_AMOLED_MODE] = settings.amoledMode
            prefs[KEY_HAPTIC_ENABLED] = settings.hapticEnabled
            if (settings.pin != null) {
                prefs[KEY_PIN] = settings.pin
            } else {
                prefs.remove(KEY_PIN)
            }
            prefs[KEY_DEVICE_NAME] = settings.deviceName
            prefs[KEY_MAX_CONNECTIONS] = settings.maxConnections
            prefs[KEY_ENABLE_NEARBY] = settings.enableNearbyDiscovery
            prefs[KEY_ONBOARDING_COMPLETED] = settings.onboardingCompleted
            prefs[KEY_ENCRYPTION_ENABLED] = settings.encryptionEnabled
        }
    }
}
