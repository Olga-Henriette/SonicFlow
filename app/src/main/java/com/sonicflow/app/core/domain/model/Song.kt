package com.sonicflow.app.core.domain.model

import android.net.Uri

data class Song(
    val id: Long,                    // MediaStore ID
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,              // Durée en millisecondes
    val path: String,                // Chemin complet du fichier
    val uri: Uri,                    // Uri pour ExoPlayer
    val size: Long,                  // Taille en bytes
    val mimeType: String,            // "audio/mpeg", "audio/flac", ...
    val dateAdded: Long,             // Timestamp d'ajout
    val dateModified: Long,
    val track: Int = 0,              // Numéro de piste dans l'album
    val year: Int = 0,
    val genre: String? = null,
    val bitrate: Int = 0,            // kbps
    val sampleRate: Int = 0,         // Hz
    val isFavorite: Boolean = false  // Marqué favori (local)
) {
    companion object {
        // Chanson vide pour états initiaux
        val EMPTY = Song(
            id = -1,
            title = "",
            artist = "",
            album = "",
            albumId = -1,
            duration = 0,
            path = "",
            uri = Uri.EMPTY,
            size = 0,
            mimeType = "",
            dateAdded = 0,
            dateModified = 0
        )
    }
}