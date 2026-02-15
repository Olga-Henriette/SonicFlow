package com.sonicflow.app.feature.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.ui.components.AlbumArtImage

/**
 * Carte d'album dans une grille
 * Composant réutilisable pour afficher un album
 */
@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Album art
            AlbumArtImage(
                albumId = album.id,
                contentDescription = album.name,
                size = 140.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Album name
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2 // Force 2 lignes pour alignement
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Artist
            Text(
                text = album.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Song count
            Text(
                text = "${album.songCount} songs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}