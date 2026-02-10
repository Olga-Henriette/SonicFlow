package com.sonicflow.app.core.common

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.asImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Extrait les couleurs dominantes d'une image d'album
 */
object PaletteExtractor {

    /**
     * Extrait la palette de couleurs d'un album
     */
    suspend fun extractPalette(
        context: Context,
        imageLoader: ImageLoader,
        albumId: Long
    ): AlbumPalette? = withContext(Dispatchers.IO) {
        try {
            val uri = AlbumArtLoader(context).getAlbumArtUri(albumId)

            val request = ImageRequest.Builder(context)
                .data(uri)
                .size(Size.ORIGINAL)
                .build()

            val result = imageLoader.execute(request)

            if (result is SuccessResult) {
                // Utiliser image au lieu de drawable
                val bitmap = result.image.toBitmap()

                val palette = Palette.from(bitmap).generate()

                AlbumPalette(
                    vibrant = palette.vibrantSwatch?.let {
                        Color(it.rgb)
                    },
                    darkVibrant = palette.darkVibrantSwatch?.let {
                        Color(it.rgb)
                    },
                    lightVibrant = palette.lightVibrantSwatch?.let {
                        Color(it.rgb)
                    },
                    muted = palette.mutedSwatch?.let {
                        Color(it.rgb)
                    },
                    darkMuted = palette.darkMutedSwatch?.let {
                        Color(it.rgb)
                    },
                    lightMuted = palette.lightMutedSwatch?.let {
                        Color(it.rgb)
                    },
                    dominant = palette.dominantSwatch?.let {
                        Color(it.rgb)
                    }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract palette for album $albumId")
            null
        }
    }
}

/**
 * Palette de couleurs extraites d'un album
 */
data class AlbumPalette(
    val vibrant: Color? = null,
    val darkVibrant: Color? = null,
    val lightVibrant: Color? = null,
    val muted: Color? = null,
    val darkMuted: Color? = null,
    val lightMuted: Color? = null,
    val dominant: Color? = null
) {
    /**
     * Couleur principale (préférence : vibrant > dominant > muted)
     */
    val primary: Color
        get() = vibrant ?: dominant ?: muted ?: Color(0xFF1DB954) // Fallback green

    /**
     * Couleur de fond (préférence : darkMuted > darkVibrant)
     */
    val background: Color
        get() = darkMuted ?: darkVibrant ?: Color(0xFF121212) // Fallback dark
}