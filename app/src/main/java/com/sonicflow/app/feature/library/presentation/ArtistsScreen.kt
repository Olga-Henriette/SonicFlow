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

/**
 * Écran de la liste d'artistes
 * Accepte les artistes filtrés
 */
@Composable
fun ArtistsScreen(
    filteredArtists: List<Artist>? = null,
    onArtistClick: (Artist) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val songs by viewModel.songs.collectAsState()

    // Utiliser filteredArtists si fourni
    val artists = filteredArtists ?: remember(songs) {
        songs
            .groupBy { normalizeArtistName(it.artist) }
            .map { (normalizedName, artistSongs) ->
                val displayName = artistSongs
                    .groupBy { it.artist }
                    .maxByOrNull { it.value.size }
                    ?.key ?: normalizedName

                val albums = artistSongs.map { it.albumId }.distinct()

                Artist(
                    id = normalizedName.hashCode().toLong(),
                    name = displayName,
                    albumCount = albums.size,
                    songCount = artistSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    if (artists.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (filteredArtists != null) "No artists found" else "No artists in library",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (filteredArtists != null) {
                    Text(
                        text = "Try a different search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(artists, key = { it.id }) { artist ->
                ArtistItem(
                    artist = artist,
                    onClick = { onArtistClick(artist) }
                )
            }
        }
    }
}

private fun normalizeArtistName(artist: String): String {
    return artist
        .lowercase()
        .trim()
        .split(
            " feat ",
            " feat. ",
            " ft ",
            " ft. ",
            " featuring ",
            " & ",
            " and ",
            " x ",
            " - ",
            " with "
        )
        .first()
        .trim()
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