package com.sonicflow.app.core.data.repository

import com.sonicflow.app.core.data.preferences.PreferencesDataStore
import com.sonicflow.app.core.data.preferences.PreferencesKeys
import com.sonicflow.app.core.domain.model.*
import com.sonicflow.app.core.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: PreferencesDataStore
) : PreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.preferencesFlow.map { prefs ->
        UserPreferences(
            themeMode = ThemeMode.fromString(
                prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            ),
            accentColor = AccentColor.fromString(
                prefs[PreferencesKeys.ACCENT_COLOR] ?: AccentColor.DYNAMIC.name
            ),
            customTheme = CustomTheme(
                isEnabled = prefs[PreferencesKeys.CUSTOM_THEME_ENABLED] ?: false,
                imageUri = prefs[PreferencesKeys.THEME_IMAGE_URI],
                isGradient = prefs[PreferencesKeys.THEME_IS_GRADIENT] ?: false,
                primaryColor = prefs[PreferencesKeys.THEME_PRIMARY_COLOR],
                secondaryColor = prefs[PreferencesKeys.THEME_SECONDARY_COLOR],
                blurAmount = prefs[PreferencesKeys.THEME_BLUR_AMOUNT] ?: 0f,
                alpha = prefs[PreferencesKeys.THEME_ALPHA] ?: 1f
            ),
            recentPlayLimit = prefs[PreferencesKeys.RECENT_PLAY_LIMIT] ?: 10,
            recentAddLimit = prefs[PreferencesKeys.RECENT_ADD_LIMIT] ?: 10,
            mostPlayedLimit = prefs[PreferencesKeys.MOST_PLAYED_LIMIT] ?: 10,
            fontSettings = FontSettings(
                style = AppFontStyle.fromString(
                    prefs[PreferencesKeys.FONT_STYLE] ?: AppFontStyle.DEFAULT.name
                ),
                sizeScale = prefs[PreferencesKeys.FONT_SIZE_SCALE] ?: 1.0f
            ),
            languageCode = prefs[PreferencesKeys.LANGUAGE_CODE] ?: "system",
            widgetBackgroundOpacity = prefs[PreferencesKeys.WIDGET_BACKGROUND_OPACITY] ?: 0.8f,
            widgetShowAlbumArt = prefs[PreferencesKeys.WIDGET_SHOW_ALBUM_ART] ?: true,
            widgetCornerRadius = prefs[PreferencesKeys.WIDGET_CORNER_RADIUS] ?: 16
        )
    }

    override suspend fun updateThemeMode(mode: String) {
        dataStore.savePreference(PreferencesKeys.THEME_MODE, mode)
        Timber.d("Theme mode updated: $mode")
    }

    override suspend fun updateAccentColor(color: String) {
        dataStore.savePreference(PreferencesKeys.ACCENT_COLOR, color)
        Timber.d("Accent color updated: $color")
    }

    override suspend fun updateCustomTheme(
        enabled: Boolean,
        imageUri: String?,
        isGradient: Boolean,
        primaryColor: String?,
        secondaryColor: String?,
        blurAmount: Float,
        alpha: Float
    ) {
        dataStore.savePreference(PreferencesKeys.CUSTOM_THEME_ENABLED, enabled)
        imageUri?.let { dataStore.savePreference(PreferencesKeys.THEME_IMAGE_URI, it) }
        dataStore.savePreference(PreferencesKeys.THEME_IS_GRADIENT, isGradient)
        primaryColor?.let { dataStore.savePreference(PreferencesKeys.THEME_PRIMARY_COLOR, it) }
        secondaryColor?.let { dataStore.savePreference(PreferencesKeys.THEME_SECONDARY_COLOR, it) }
        dataStore.savePreference(PreferencesKeys.THEME_BLUR_AMOUNT, blurAmount)
        dataStore.savePreference(PreferencesKeys.THEME_ALPHA, alpha)
        Timber.d("Custom theme updated")
    }

    override suspend fun updateLimits(recentPlay: Int, recentAdd: Int, mostPlayed: Int) {
        dataStore.savePreference(PreferencesKeys.RECENT_PLAY_LIMIT, recentPlay)
        dataStore.savePreference(PreferencesKeys.RECENT_ADD_LIMIT, recentAdd)
        dataStore.savePreference(PreferencesKeys.MOST_PLAYED_LIMIT, mostPlayed)
        Timber.d("Limits updated: recent=$recentPlay, add=$recentAdd, played=$mostPlayed")
    }

    override suspend fun updateFontSettings(style: String, sizeScale: Float) {
        dataStore.savePreference(PreferencesKeys.FONT_STYLE, style)
        dataStore.savePreference(PreferencesKeys.FONT_SIZE_SCALE, sizeScale)
        Timber.d("Font settings updated: style=$style, scale=$sizeScale")
    }

    override suspend fun updateLanguage(code: String) {
        dataStore.savePreference(PreferencesKeys.LANGUAGE_CODE, code)
        Timber.d("Language updated: $code")
    }

    override suspend fun updateWidgetSettings(opacity: Float, showAlbumArt: Boolean, cornerRadius: Int) {
        dataStore.savePreference(PreferencesKeys.WIDGET_BACKGROUND_OPACITY, opacity)
        dataStore.savePreference(PreferencesKeys.WIDGET_SHOW_ALBUM_ART, showAlbumArt)
        dataStore.savePreference(PreferencesKeys.WIDGET_CORNER_RADIUS, cornerRadius)
        Timber.d("Widget settings updated")
    }

    override suspend fun resetToDefaults() {
        dataStore.clearAll()
        Timber.d("Preferences reset to defaults")
    }
}