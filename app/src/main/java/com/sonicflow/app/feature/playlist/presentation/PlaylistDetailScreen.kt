package com.sonicflow.app.feature.playlist.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.feature.library.presentation.SongItem
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: PlaylistDetailViewModel = hiltViewModel()

    val playlistSongs by viewModel.getPlaylistSongsUseCase(playlist.id)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Play all
                    if (playlistSongs.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                playerViewModel.handleIntent(
                                    PlayerIntent.PlayQueue(playlistSongs, 0)
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
        if (playlistSongs.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No songs in this playlist",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                item {
                    PlaylistHeader(
                        playlist = playlist,
                        songCount = playlistSongs.size
                    )
                 }

                // Songs
                items(playlistSongs) { song ->
                    SongItem(
                        song = song,
                        onFavoriteClick = { clickedSong ->
                            playerViewModel.handleIntent(
                                PlayerIntent.ToggleFavorite(clickedSong.id)
                            )
                        },
                        onClick = {
                            val index = playlistSongs.indexOf(song)
                            playerViewModel.handleIntent(
                                PlayerIntent.PlayQueue(playlistSongs, index)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(
    playlist: Playlist,
    songCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$songCount songs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
}