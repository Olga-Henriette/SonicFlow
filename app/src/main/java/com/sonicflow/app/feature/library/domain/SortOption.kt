package com.sonicflow.app.feature.library.domain

enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    ARTIST_ASC("Artist (A-Z)"),
    ARTIST_DESC("Artist (Z-A)"),
    DATE_ADDED_DESC("Recently Added"),
    DATE_ADDED_ASC("Oldest First"),
    DURATION_DESC("Longest First"),
    DURATION_ASC("Shortest First");

    companion object {
        val DEFAULT = NAME_ASC
    }
}