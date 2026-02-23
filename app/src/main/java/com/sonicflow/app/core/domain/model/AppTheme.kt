package com.sonicflow.app.core.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String): ThemeMode {
            return values().find { it.name == value } ?: SYSTEM
        }
    }
}

data class CustomTheme(
    val isEnabled: Boolean = false,
    val imageUri: String? = null,
    val isGradient: Boolean = false,
    val primaryColor: String? = null,
    val secondaryColor: String? = null,
    val blurAmount: Float = 0f, // 0 - 25
    val alpha: Float = 1f // 0 - 1
) {
    companion object {
        val DEFAULT = CustomTheme()
    }
}