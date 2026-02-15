package com.sonicflow.app.feature.library.presentation


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.AlbumPalette
import com.sonicflow.app.core.common.formatDuration
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.AlbumArtImage
import com.sonicflow.app.feature.library.components.SearchTopBar
import com.sonicflow.app.feature.library.components.SortDialog
import com.sonicflow.app.feature.library.components.TabStatsBar
import com.sonicflow.app.feature.player.components.MiniPlayer
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import com.sonicflow.app.feature.playlist.presentation.PlaylistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    albumPalette: AlbumPalette? = null,
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
    val sortOption by viewModel.sortOption.collectAsState()

    val albumCount by viewModel.albumCount.collectAsState()
    val artistCount by viewModel.artistCount.collectAsState()
    val playlistCount by playlistViewModel.playlistCount.collectAsState()

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("For You", "Songs", "Favorites", "Playlists", "Albums", "Artists")

    val pagerState = rememberPagerState(
        initialPage = initialTab,
        pageCount = { tabs.size }
    )

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedTab) {
        onTabChanged(selectedTab)
    }

    LaunchedEffect(pagerState.currentPage) {
        onTabChanged(pagerState.currentPage)
    }

    val favoriteSongs = songs.filter { it.isFavorite }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showDrawer by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    val filteredSongs = if (searchQuery.isBlank()) {
        songs
    } else {
        songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    // État pour pull-to-refresh
    var isRefreshing by remember { mutableStateOf(false) }

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
                        title = { Text("SonicFlow") },
                        navigationIcon = {
                            // Menu burger
                            IconButton(onClick = { showDrawer = true }) {
                                Icon(Icons.Default.Menu, "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, "Search")
                            }
                            IconButton(onClick = { /* TODO: Options */ }) {
                                Icon(Icons.Default.MoreVert, "Options")
                            }
                        }
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        )
                    }
                }

                // Stats et tri
                TabStatsBar(
                    currentTab = pagerState.currentPage,
                    songsCount = songs.size,
                    favoritesCount = favoriteSongs.size,
                    playlistsCount = playlistCount,
                    albumsCount = albumCount,
                    artistsCount = artistCount,
                    onSortClick = if (pagerState.currentPage in 1..2) {
                        { showSortDialog = true }
                    } else null
                )
            }
        },
        bottomBar = {
            MiniPlayer(
                currentSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                albumPalette = albumPalette,
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
        LibraryPagerContent(
            pagerState = pagerState,
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            error = error,
            filteredSongs = filteredSongs,
            favoriteSongs = favoriteSongs,
            playerViewModel = playerViewModel,
            currentSong = playerState.currentSong,
            isPlaying = playerState.isPlaying,
            forYouViewModel = hiltViewModel(),
            onSongClick = onSongClick,
            onPlaylistClick = onPlaylistClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    viewModel.loadSongs()
                    kotlinx.coroutines.delay(1000)
                    isRefreshing = false
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
    if (showSortDialog) {
        SortDialog(
            currentOption = sortOption,
            onOptionSelected = { option ->
                viewModel.setSortOption(option)
            },
            onDismiss = { showSortDialog = false }
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
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}