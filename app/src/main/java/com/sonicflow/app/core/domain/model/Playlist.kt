package com.sonicflow.app.core.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val dateCreated: Long,
    val dateModified: Long,
    val artworkUri: String? = null  // Artwork de la 1ère chanson ou custom
) {
    companion object {
        // Playlists système (auto-générées)
        const val FAVORITES_ID = -1L
        const val RECENTLY_PLAYED_ID = -2L
        const val MOST_PLAYED_ID = -3L
        const val RECENTLY_ADDED_ID = -4L
    }
}