package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import com.sonicflow.app.core.ui.components.AlbumArtImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.formatDuration
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.platform.LocalContext
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.feature.playlist.components.AddToPlaylistDialog
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.usecase.GetMostPlayedUseCase
import com.sonicflow.app.core.domain.usecase.GetRecentlyPlayedUseCase
import com.sonicflow.app.feature.playlist.presentation.PlaylistsScreen
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerState
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import com.sonicflow.app.feature.playlist.components.AddToPlaylistDialog
import com.sonicflow.app.feature.playlist.presentation.CreatePlaylistDialog
import com.sonicflow.app.feature.playlist.presentation.PlaylistViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    initialTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    onPlaylistClick: (Playlist) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onMiniPlayerClick: () -> Unit = {}
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val playerState by playerViewModel.state.collectAsState()

    // Gestion des onglets
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("For You","Songs", "Favorites", "Playlists","Albums", "Artists")

    LaunchedEffect(selectedTab) {
        onTabChanged(selectedTab)
    }

    // Filtrer les favoris
    val favoriteSongs = songs.filter { it.isFavorite }

    // État de recherche
    var searchQuery by remember { mutableStateOf("") }
    //val debouncedQuery = rememberDebouncedValue(searchQuery, 300L)

    var isSearchActive by remember { mutableStateOf(false) }

    // Filtrer selon la recherche
    val filteredSongs = if (searchQuery.isBlank()) {
        songs
    } else {
        songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Column {
                if (isSearchActive) {
                    SearchTopBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onCloseSearch = {
                            isSearchActive = false
                            searchQuery = ""
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("Library") },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                        }
                    )
                }

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
                        0 -> {
                            val forYouViewModel: ForYouViewModel = hiltViewModel()
                            ForYouScreen(
                                libraryViewModel = viewModel,
                                playerViewModel = playerViewModel,
                                getMostPlayedUseCase = forYouViewModel.getMostPlayedUseCase,
                                getRecentlyPlayedUseCase = forYouViewModel.getRecentlyPlayedUseCase,
                                onSongClick = onSongClick
                            )
                        }
                        1 -> {
                            SongsList(
                                songs = filteredSongs,
                                playerViewModel = playerViewModel,
                                onSongClick = onSongClick
                            )
                        }
                        2 -> { // Favorites
                            SongsList(
                                songs = favoriteSongs,
                                playerViewModel = playerViewModel,
                                onSongClick = onSongClick,
                                emptyMessage = "No favorite songs yet"
                            )
                        }
                        3 -> { // Playlists
                            PlaylistsScreen(
                                onPlaylistClick = onPlaylistClick
                            )
                        }
                        4 -> { // Albums
                            AlbumsScreen(
                                onAlbumClick = onAlbumClick
                            )
                        }
                        5 -> { // Artists
                            ArtistsScreen(
                                onArtistClick = onArtistClick
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search songs, artists, albums...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.ArrowBack, "Close search")
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        }
    )
}

// Extraire la liste en composable séparé
@Composable
fun SongsList(
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onSongClick: (Song, List<Song>) -> Unit,
    emptyMessage: String = "No songs found"
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    val context = LocalContext.current

    var songToAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

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
                    onAddToPlaylistClick = { clickedSong ->
                        songToAddToPlaylist = clickedSong
                    },
                    onClick = { onSongClick(song, songs) }
                )
            }
        }
    }
    // Dialog pour ajouter à une playlist
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

    // Dialog création rapide de playlist
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
@Composable
fun SongItem(
    song: Song,
    onFavoriteClick: (Song) -> Unit = {},
    onAddToPlaylistClick: (Song) -> Unit = {},
    onMoreClick: (Song) -> Unit = {},
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
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                var showMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add to playlist") },
                            leadingIcon = {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                onAddToPlaylistClick(song)
                            }
                        )
                    }
                }

                Text(
                    text = song.duration.formatDuration(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // More (menu)
            IconButton(
                onClick = { onMoreClick(song) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

