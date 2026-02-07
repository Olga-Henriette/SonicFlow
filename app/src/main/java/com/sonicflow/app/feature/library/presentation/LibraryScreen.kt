package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.ui.components.AlbumArtImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.formatDuration
import androidx.compose.foundation.clickable
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerState
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onMiniPlayerClick: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    // Gestion des onglets
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Favorites", "Albums", "Artists")

    // Filtrer les favoris
    val favoriteSongs = songs.filter { it.isFavorite }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Library") }
                )
                TabRow(
                    selectedTabIndex = selectedTab
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    // Afficher selon l'onglet sélectionné
                    when (selectedTab) {
                        0 -> SongsList(
                            songs = songs,
                            playerViewModel = playerViewModel,
                            onSongClick = onSongClick
                        )
                        1 -> SongsList(
                            songs = favoriteSongs,
                            playerViewModel = playerViewModel,
                            onSongClick = onSongClick,
                            emptyMessage = "No favorite songs yet"
                        )
                        2 -> Text(
                            text = "Albums - Coming soon",
                            modifier = Modifier.align(Alignment.Center)
                        )
                        3 -> Text(
                            text = "Artists - Coming soon",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

// Extraire la liste en composable séparé
@Composable
fun SongsList(
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    emptyMessage: String = "No songs found"
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emptyMessage)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(songs) { song ->
                SongItem(
                    song = song,
                    onFavoriteClick = { clickedSong ->
                        playerViewModel.handleIntent(
                            PlayerIntent.ToggleFavorite(clickedSong.id)
                        )
                    },
                    onClick = { onSongClick(song, songs) }
                )
            }
        }
    }
}
@Composable
fun SongItem(
    song: Song,
    onFavoriteClick: (Song) -> Unit = {},
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${song.artist} • ${song.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AlbumArtImage(
                albumId = song.albumId,
                contentDescription = song.album,
                size = 56.dp
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onFavoriteClick(song) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (song.isFavorite) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Text(
                    text = song.duration.formatDuration(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

