// feature/library/presentation/AlbumDetailScreen.kt

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
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.ui.components.AlbumArtImage
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun AlbumDetailScreen(
    album: Album,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onMiniPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val allSongs by libraryViewModel.songs.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    // Filtrer les chansons de cet album
    val albumSongs = remember(allSongs, album.id) {
        allSongs.filter { it.albumId == album.id }
            .sortedBy { it.track }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (albumSongs.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                playerViewModel.handleIntent(
                                    PlayerIntent.PlayQueue(albumSongs, 0)
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
            // Header avec artwork
            item {
                AlbumHeader(album = album)
            }

            // Liste des chansons
            items(albumSongs) { song ->
                SongItem(
                    song = song,
                    onFavoriteClick = { clickedSong ->
                        playerViewModel.handleIntent(
                            PlayerIntent.ToggleFavorite(clickedSong.id)
                        )
                    },
                    onClick = {
                        val index = albumSongs.indexOf(song)
                        playerViewModel.handleIntent(
                            PlayerIntent.PlayQueue(albumSongs, index)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AlbumHeader(
    album: Album,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Album art
        AlbumArtImage(
            albumId = album.id,
            contentDescription = album.name,
            size = 200.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Album name
        Text(
            text = album.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Artist
        Text(
            text = album.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Info
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (album.year > 0) {
                Text(
                    text = album.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•")
            }
            Text(
                text = "${album.songCount} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}