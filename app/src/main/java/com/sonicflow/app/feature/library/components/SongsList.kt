package com.sonicflow.app.feature.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.SongListItem
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import com.sonicflow.app.feature.playlist.components.AddToPlaylistDialog
import com.sonicflow.app.feature.playlist.presentation.CreatePlaylistDialog
import com.sonicflow.app.feature.playlist.presentation.PlaylistViewModel

/**
 * Liste de chansons réutilisable
 * Composant professionnel avec gestion d'état complète
 */
@Composable
fun SongsList(
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit,
    emptyMessage: String = "No songs found",
    modifier: Modifier = Modifier
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    val context = LocalContext.current

    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    if (songs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emptyMessage)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize()
        ) {
            items(songs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isCurrentlyPlaying = song.id == currentSong?.id,
                    isPlaying = isPlaying,
                    onSongClick = { onSongClick(song, songs) },
                    onFavoriteClick = {
                        playerViewModel.handleIntent(
                            PlayerIntent.ToggleFavorite(song.id)
                        )
                    },
                    onMoreClick = {
                        songToAddToPlaylist = song
                    }
                )
            }
        }
    }

    // Dialogs
    songToAddToPlaylist?.let { song ->
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { songToAddToPlaylist = null },
            onPlaylistSelected = { playlist ->
                playerViewModel.handleIntent(
                    PlayerIntent.AddToPlaylist(
                        playlistId = playlist.id,
                        songId = song.id
                    )
                )
                context.showToast("Added to ${playlist.name}")
                songToAddToPlaylist = null
            },
            onCreateNewPlaylist = {
                songToAddToPlaylist = null
                showCreatePlaylistDialog = true
            }
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                playlistViewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
}