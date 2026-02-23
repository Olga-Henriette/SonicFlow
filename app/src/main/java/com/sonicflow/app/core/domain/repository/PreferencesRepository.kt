package com.sonicflow.app.core.domain.repository

import com.sonicflow.app.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun updateThemeMode(mode: String)
    suspend fun updateAccentColor(color: String)
    suspend fun updateCustomTheme(
        enabled: Boolean,
        imageUri: String? = null,
        isGradient: Boolean = false,
        primaryColor: String? = null,
        secondaryColor: String? = null,
        blurAmount: Float = 0f,
        alpha: Float = 1f
    )
    suspend fun updateLimits(recentPlay: Int, recentAdd: Int, mostPlayed: Int)
    suspend fun updateFontSettings(style: String, sizeScale: Float)
    suspend fun updateLanguage(code: String)
    suspend fun updateWidgetSettings(opacity: Float, showAlbumArt: Boolean, cornerRadius: Int)
    suspend fun resetToDefaults()
}