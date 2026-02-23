package com.sonicflow.app.core.domain.model

data class UserPreferences(
    // Theme
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DYNAMIC,
    val customTheme: CustomTheme = CustomTheme.DEFAULT,

    // Limits
    val recentPlayLimit: Int = 10,
    val recentAddLimit: Int = 10,
    val mostPlayedLimit: Int = 10,

    // Display
    val fontSettings: FontSettings = FontSettings.DEFAULT,

    // Language
    val languageCode: String = "system", // "system", "en", "fr", etc.

    // Widgets
    val widgetBackgroundOpacity: Float = 0.8f,
    val widgetShowAlbumArt: Boolean = true,
    val widgetCornerRadius: Int = 16
) {
    companion object {
        val DEFAULT = UserPreferences()
    }
}