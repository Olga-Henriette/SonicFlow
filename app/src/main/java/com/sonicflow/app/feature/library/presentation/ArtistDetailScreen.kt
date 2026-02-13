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
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onMiniPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val allSongs by libraryViewModel.songs.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    val artistSongs = remember(allSongs, artist.name) {
        allSongs.filter {
            normalizeArtistName(it.artist) == normalizeArtistName(artist.name)
        }.sortedWith(compareBy({ it.album }, { it.track }))
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
        },
        bottomBar = {
            MiniPlayer(
                currentSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                onPlayPauseClick = {
                    playerViewModel.handleIntent(PlayerIntent.PlayPause)
                },
                onNextClick = {
                    playerViewModel.handleIntent(PlayerIntent.Next)
                },
                onMiniPlayerClick = onMiniPlayerClick
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
                    com.sonicflow.app.core.ui.components.SongListItem( // ← Utiliser le nouveau composant
                        song = song,
                        isCurrentlyPlaying = song.id == playerState.currentSong?.id,
                        isPlaying = playerState.isPlaying,
                        onSongClick = {
                            val index = artistSongs.indexOf(song)
                            playerViewModel.handleIntent(
                                PlayerIntent.PlayQueue(artistSongs, index)
                            )
                        },
                        onFavoriteClick = {
                            playerViewModel.handleIntent(
                                PlayerIntent.ToggleFavorite(song.id)
                            )
                        },
                        onMoreClick = {
                            // TODO: Add to playlist
                        }
                    )
                }
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