package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Artist

@Composable
fun ArtistsScreen(
    onArtistClick: (Artist) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = hiltViewModel()

    val songs by viewModel.songs.collectAsState()

    // Générer la liste d'artistes depuis les chansons
    val artists = remember(songs) {
        songs
            .groupBy { it.artist }
            .map { (artistName, artistSongs) ->
                val albums = artistSongs.map { it.album }.distinct()
                Artist(
                    id = artistName.hashCode().toLong(), // ID basique pour l'instant
                    name = artistName,
                    albumCount = albums.size,
                    songCount = artistSongs.size
                )
            }
            .sortedBy { it.name }
    }

    if (artists.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No artists found")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(artists) { artist ->
                ArtistItem(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

@Composable
fun ArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = artist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${artist.albumCount} albums • ${artist.songCount} songs",
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}