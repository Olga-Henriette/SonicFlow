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
import com.sonicflow.app.feature.library.components.AlbumGridItem

@Composable
fun AlbumsScreen(
    onAlbumClick: (Album) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val songs by viewModel.songs.collectAsState()

    val albums = remember(songs) {
        songs
            .groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                val firstSong = albumSongs.first()
                Album(
                    id = albumId,
                    name = firstSong .album,
                    artist = firstSong .artist,
                    artistId = 0L, // TODO: récupérer depuis MediaStore
                    songCount = albumSongs.size,
                    year = firstSong .year
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
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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