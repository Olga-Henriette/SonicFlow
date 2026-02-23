package com.sonicflow.app.feature.library.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.ui.components.DetailScreenTopBar
import com.sonicflow.app.core.ui.components.DetailInfoHeader
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

    // Normaliser le nom de l'album
    val normalizedAlbumName = remember(album.name) {
        normalizeAlbumName(album.name)
    }

    // Filtrer les chansons de cet album
    val albumSongs = remember(allSongs, normalizedAlbumName) {
        allSongs.filter {
            normalizeAlbumName(it.album) == normalizedAlbumName
        }.sortedBy { it.track }
    }

    // Gérer le bouton retour système
    BackHandler(onBack = onNavigateBack)

    // Vérifier si l'album est en train de jouer
    val isAlbumPlaying = remember(playerState.currentSong, playerState.isPlaying, albumSongs) {
        playerState.isPlaying && albumSongs.any { it.id == playerState.currentSong?.id }
    }

    Scaffold(
        topBar = {
            DetailScreenTopBar(
                title = cleanName(album.name),
                onNavigateBack = onNavigateBack,
                isPlaying = isAlbumPlaying,
                canPlay = albumSongs.isNotEmpty(),
                onPlayPauseClick = if (albumSongs.isNotEmpty()) {
                    {
                        if (isAlbumPlaying) {
                            playerViewModel.handleIntent(PlayerIntent.PlayPause)
                        } else {
                            playerViewModel.handleIntent(PlayerIntent.PlayQueue(albumSongs, 0))
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header d'info
            item {
                DetailInfoHeader(
                    title = cleanName(album.name),
                    subtitle = cleanName(album.artist),
                    info = buildString {
                        if (album.year > 0) {
                            append("${album.year} • ")
                        }
                        append("${albumSongs.size} songs")
                    },
                    albumId = album.id
                )
            }

            // Liste des chansons
            items(albumSongs, key = { it.id }) { song ->
                com.sonicflow.app.core.ui.components.SongListItem(
                    song = song,
                    isCurrentlyPlaying = song.id == playerState.currentSong?.id,
                    isPlaying = playerState.isPlaying,
                    onSongClick = {
                        val index = albumSongs.indexOf(song)
                        playerViewModel.handleIntent(
                            PlayerIntent.PlayQueue(albumSongs, index)
                        )
                    },
                    onFavoriteClick = {
                        playerViewModel.handleIntent(
                            PlayerIntent.ToggleFavorite(song.id)
                        )
                    },
                    onMoreClick = { }
                )
            }
        }
    }
}

private fun normalizeAlbumName(album: String): String {
    return album.lowercase().trim()
        .split(
            " feat ", " feat. ", " ft ", " ft. ", " featuring ",
            " & ", " and ", " x ", " - ", " with "
        )
        .first().trim()
}

private fun cleanName(name: String): String {
    return name
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
