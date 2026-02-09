package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val allSongs by libraryViewModel.songs.collectAsState()

    // Filtrer les chansons de cet artiste
    val artistSongs = remember(allSongs, artist.name) {
        allSongs.filter { it.artist.equals(artist.name, ignoreCase = true) }
            .sortedWith(compareBy({ it.album }, { it.track }))
    }

    // Grouper par album
    val albumGroups = remember(artistSongs) {
        artistSongs.groupBy { it.album }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artist.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (artistSongs.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                playerViewModel.handleIntent(
                                    PlayerIntent.PlayQueue(artistSongs, 0)
                                )
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, "Play all")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            item {
                ArtistHeader(artist = artist)
            }

            // Albums groupés
            albumGroups.forEach { (albumName, songs) ->
                item {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                    )
                }

                items(songs) { song ->
                    SongItem(
                        song = song,
                        onFavoriteClick = { clickedSong ->
                            playerViewModel.handleIntent(
                                PlayerIntent.ToggleFavorite(clickedSong.id)
                            )
                        },
                        onClick = {
                            val index = artistSongs.indexOf(song)
                            playerViewModel.handleIntent(
                                PlayerIntent.PlayQueue(artistSongs, index)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistHeader(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${artist.albumCount} albums • ${artist.songCount} songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
}