// feature/library/presentation/AlbumsScreen.kt

package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.ui.components.AlbumArtImage

@Composable
fun AlbumsScreen(
    onAlbumClick: (Album) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = hiltViewModel()

    // Pour l'instant, on génère la liste d'albums depuis les chansons
    val songs by viewModel.songs.collectAsState()
    val albums = remember(songs) {
        songs
            .groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                Album(
                    id = albumId,
                    name = albumSongs.first().album,
                    artist = albumSongs.first().artist,
                    artistId = 0L, // TODO: récupérer depuis MediaStore
                    songCount = albumSongs.size,
                    year = albumSongs.first().year
                )
            }
            .sortedBy { it.name }
    }

    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No albums found")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(albums) { album ->
                AlbumGridItem(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Album art
            AlbumArtImage(
                albumId = album.id,
                contentDescription = album.name,
                modifier = Modifier.fillMaxWidth(),
                size = 160.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Album name
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}