package com.sonicflow.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonicflow.app.core.common.AlbumArtLoader

/**
 * Composable pour afficher l'artwork d'un album
 * - Charge l'image via Coil 3
 * - Affiche un placeholder si pas d'image
 */
@Composable
fun AlbumArtImage(
    albumId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    val albumArtLoader = AlbumArtLoader(context)

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(albumArtLoader.getAlbumArtUri(albumId))
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.small),
        loading = {
            // Placeholder pendant le chargement
            Box(
                modifier = Modifier
                    .size(size)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        error = {
            // Placeholder si erreur
            Box(
                modifier = Modifier
                    .size(size)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size / 2)
                )
            }
        }
    )
}