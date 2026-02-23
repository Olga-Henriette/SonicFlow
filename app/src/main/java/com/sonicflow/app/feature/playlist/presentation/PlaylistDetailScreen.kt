package com.sonicflow.app.feature.playlist.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.ui.components.ConfirmationDialog
import com.sonicflow.app.core.ui.components.DetailScreenTopBar
import com.sonicflow.app.core.ui.components.DetailInfoHeader
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onMiniPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: PlaylistDetailViewModel = hiltViewModel()
    val playlistSongs by viewModel.getPlaylistSongsUseCase(playlist.id)
        .collectAsState(initial = emptyList())
    val playerState by playerViewModel.state.collectAsState()
    val context = LocalContext.current

    var songToRemove by remember { mutableStateOf<com.sonicflow.app.core.domain.model.Song?>(null) }

    BackHandler(onBack = onNavigateBack)

    val isPlaylistPlaying = remember(playerState.currentSong, playerState.isPlaying, playlistSongs) {
        playerState.isPlaying && playlistSongs.any { it.id == playerState.currentSong?.id }
    }

    Scaffold(
        topBar = {
            DetailScreenTopBar(
                title = playlist.name,
                onNavigateBack = onNavigateBack,
                isPlaying = isPlaylistPlaying,
                canPlay = playlistSongs.isNotEmpty(),
                onPlayPauseClick = if (playlistSongs.isNotEmpty()) {
                    {
                        if (isPlaylistPlaying) {
                            playerViewModel.handleIntent(PlayerIntent.PlayPause)
                        } else {
                            playerViewModel.handleIntent(PlayerIntent.PlayQueue(playlistSongs, 0))
                        }
                    }
                } else null,
                onSearchClick = { /* TODO */ }
            )
        },
        bottomBar = {
            MiniPlayer(
                currentSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                hasNext = playerState.hasNext,
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
                item {
                    DetailInfoHeader(
                        title = playlist.name,
                        info = "${playlistSongs.size} songs",
                        icon = Icons.Default.QueueMusic,
                        artworkSize = 120.dp
                    )
                }

                items(playlistSongs, key = { it.id }) { song ->
                    com.sonicflow.app.core.ui.components.SongListItem(
                        song = song,
                        isCurrentlyPlaying = song.id == playerState.currentSong?.id,
                        isPlaying = playerState.isPlaying,
                        onSongClick = {
                            val index = playlistSongs.indexOf(song)
                            playerViewModel.handleIntent(
                                PlayerIntent.PlayQueue(playlistSongs, index)
                            )
                        },
                        onFavoriteClick = {
                            playerViewModel.handleIntent(
                                PlayerIntent.ToggleFavorite(song.id)
                            )
                        },
                        onMoreClick = { songToRemove = song }
                    )
                }
            }
        }
    }

    songToRemove?.let { song ->
        ConfirmationDialog(
            title = "Remove Song",
            message = "Remove \"${song.title}\" from ${playlist.name}?",
            icon = Icons.Outlined.Delete,
            confirmText = "Remove",
            isDestructive = true,
            onConfirm = {
                playerViewModel.handleIntent(
                    PlayerIntent.RemoveFromPlaylist(playlist.id, song.id)
                )
                context.showToast("Removed from ${playlist.name}")
                songToRemove = null
            },
            onDismiss = { songToRemove = null }
        )
    }
}