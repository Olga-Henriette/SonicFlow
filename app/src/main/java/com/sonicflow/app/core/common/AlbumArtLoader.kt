package com.sonicflow.app.core.common

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilitaire pour les URIs des pochettes d'albums
 */
@Singleton
class AlbumArtLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Récupère l'URI de l'artwork d'un album
     */
    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
    }
}