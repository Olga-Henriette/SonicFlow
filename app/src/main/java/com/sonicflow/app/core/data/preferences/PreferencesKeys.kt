package com.sonicflow.app.core.data.preferences

import androidx.datastore.preferences.core.*

object PreferencesKeys {
    // Theme
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ACCENT_COLOR = stringPreferencesKey("accent_color")
    val CUSTOM_THEME_ENABLED = booleanPreferencesKey("custom_theme_enabled")
    val THEME_IMAGE_URI = stringPreferencesKey("theme_image_uri")
    val THEME_IS_GRADIENT = booleanPreferencesKey("theme_is_gradient")
    val THEME_PRIMARY_COLOR = stringPreferencesKey("theme_primary_color")
    val THEME_SECONDARY_COLOR = stringPreferencesKey("theme_secondary_color")
    val THEME_BLUR_AMOUNT = floatPreferencesKey("theme_blur_amount") // 0f - 25f
    val THEME_ALPHA = floatPreferencesKey("theme_alpha") // 0f - 1f

    // Limits
    val RECENT_PLAY_LIMIT = intPreferencesKey("recent_play_limit")
    val RECENT_ADD_LIMIT = intPreferencesKey("recent_add_limit")
    val MOST_PLAYED_LIMIT = intPreferencesKey("most_played_limit")

    // Display
    val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale") // 0.8f - 1.5f
    val FONT_STYLE = stringPreferencesKey("font_style")

    // Language
    val LANGUAGE_CODE = stringPreferencesKey("language_code")

    // Widgets
    val WIDGET_BACKGROUND_OPACITY = floatPreferencesKey("widget_bg_opacity") // 0.5f - 1f
    val WIDGET_SHOW_ALBUM_ART = booleanPreferencesKey("widget_show_album_art")
    val WIDGET_CORNER_RADIUS = intPreferencesKey("widget_corner_radius") // 0-32 dp
}