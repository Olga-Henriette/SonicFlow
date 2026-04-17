package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.feature.library.components.AlbumGridItem

/**
 * Écran de la grille d'albums
 * Accepte les albums filtrés
 */
@Composable
fun AlbumsScreen(
    filteredAlbums: List<Album>? = null,
    searchQuery: String = "",
    onAlbumClick: (Album) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val songs by viewModel.songs.collectAsState()

    val albums = filteredAlbums ?: remember(songs) {
        songs
            .groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = albumId,
                    name = firstSong.album,
                    artist = firstSong.artist,
                    artistId = 0L,
                    songCount = albumSongs.size,
                    year = firstSong.year
                )
            }
            .sortedBy { it.name }
    }

    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (filteredAlbums != null) "No albums found" else "No albums in library",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (filteredAlbums != null) {
                    Text(
                        text = "Try a different search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(albums, key = { it.id }) { album ->
                AlbumGridItem(
                    album = album,
                    searchQuery = searchQuery,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}