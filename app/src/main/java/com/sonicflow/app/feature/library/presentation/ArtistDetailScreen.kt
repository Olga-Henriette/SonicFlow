package com.sonicflow.app.feature.library.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.ui.components.DetailScreenTopBar
import com.sonicflow.app.core.ui.components.DetailInfoHeader
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

    val albumGroups = remember(artistSongs) {
        artistSongs.groupBy { it.album }
    }

    BackHandler(onBack = onNavigateBack)

    val isArtistPlaying = remember(playerState.currentSong, playerState.isPlaying, artistSongs) {
        playerState.isPlaying && artistSongs.any { it.id == playerState.currentSong?.id }
    }

    Scaffold(
        topBar = {
            DetailScreenTopBar(
                title = cleanName(artist.name),
                onNavigateBack = onNavigateBack,
                isPlaying = isArtistPlaying,
                canPlay = artistSongs.isNotEmpty(),
                onPlayPauseClick = if (artistSongs.isNotEmpty()) {
                    {
                        if (isArtistPlaying) {
                            playerViewModel.handleIntent(PlayerIntent.PlayPause)
                        } else {
                            playerViewModel.handleIntent(PlayerIntent.PlayQueue(artistSongs, 0))
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
            item {
                DetailInfoHeader(
                    title = cleanName(artist.name),
                    info = "${artist.albumCount} albums • ${artistSongs.size} songs",
                    icon = Icons.Default.Person,
                    artworkSize = 120.dp
                )
            }

            albumGroups.forEach { (albumName, songs) ->
                item {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                    )
                }

                items(songs, key = { it.id }) { song ->
                    com.sonicflow.app.core.ui.components.SongListItem(
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
                        onMoreClick = { }
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