package com.sonicflow.app.core.domain.model

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

enum class AppFontStyle(val displayName: String) {
    DEFAULT("System Default"),
    ROBOTO("Roboto"),
    OPEN_SANS("Open Sans"),
    LATO("Lato"),
    MONTSERRAT("Montserrat"),
    POPPINS("Poppins");

    companion object {
        fun fromString(value: String): AppFontStyle {
            return values().find { it.name == value } ?: DEFAULT
        }
    }
}

data class FontSettings(
    val style: AppFontStyle = AppFontStyle.DEFAULT,
    val sizeScale: Float = 1.0f // 0.8 - 1.5
) {
    companion object {
        val DEFAULT = FontSettings()
    }
}