package com.sonicflow.app.core.domain.model

import androidx.compose.ui.graphics.Color

enum class AccentColor(val displayName: String, val color: Color) {
    // Material Design 3 colors
    RED("Red", Color(0xFFB3261E)),
    PINK("Pink", Color(0xFFE91E63)),
    PURPLE("Purple", Color(0xFF9C27B0)),
    DEEP_PURPLE("Deep Purple", Color(0xFF673AB7)),
    INDIGO("Indigo", Color(0xFF3F51B5)),
    BLUE("Blue", Color(0xFF2196F3)),
    LIGHT_BLUE("Light Blue", Color(0xFF03A9F4)),
    CYAN("Cyan", Color(0xFF00BCD4)),
    TEAL("Teal", Color(0xFF009688)),
    GREEN("Green", Color(0xFF4CAF50)),
    LIGHT_GREEN("Light Green", Color(0xFF8BC34A)),
    LIME("Lime", Color(0xFFCDDC39)),
    YELLOW("Yellow", Color(0xFFFFEB3B)),
    AMBER("Amber", Color(0xFFFFC107)),
    ORANGE("Orange", Color(0xFFFF9800)),
    DEEP_ORANGE("Deep Orange", Color(0xFFFF5722)),
    BROWN("Brown", Color(0xFF795548)),
    GREY("Grey", Color(0xFF9E9E9E)),
    BLUE_GREY("Blue Grey", Color(0xFF607D8B)),
    DYNAMIC("Dynamic", Color(0xFF6750A4)); // From album art

    companion object {
        fun fromString(value: String): AccentColor {
            return values().find { it.name == value } ?: DYNAMIC
        }
    }
}