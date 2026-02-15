package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.feature.library.components.SongsList
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import com.sonicflow.app.feature.playlist.presentation.PlaylistsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Onglets scrollables de la bibliothèque
 * Composant réutilisable extrait de LibraryScreen
 */
@Composable
fun LibraryTabRow(
    pagerState: androidx.compose.foundation.pager.PagerState,
    tabs: List<String>,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 0.dp,
        modifier = modifier
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
}

/**
 * Contenu du pager de bibliothèque
 * Gère tous les onglets
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryPagerContent(
    pagerState: PagerState,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String?,
    filteredSongs: List<Song>,
    favoriteSongs: List<Song>,
    playerViewModel: PlayerViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    forYouViewModel: ForYouViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize()
    ) { page ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading && !isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    when (page) {
                        0 -> ForYouScreen(
                            forYouViewModel = forYouViewModel,
                            playerViewModel = playerViewModel,
                            getMostPlayedUseCase = forYouViewModel.getMostPlayedUseCase,
                            getRecentlyPlayedUseCase = forYouViewModel.getRecentlyPlayedUseCase,
                            onSongClick = onSongClick
                        )
                        1 -> SongsList(
                            songs = filteredSongs,
                            playerViewModel = playerViewModel,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = onSongClick
                        )
                        2 -> SongsList(
                            songs = favoriteSongs,
                            playerViewModel = playerViewModel,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            onSongClick = onSongClick,
                            emptyMessage = "No favorite songs yet"
                        )
                        3 -> PlaylistsScreen(onPlaylistClick = onPlaylistClick)
                        4 -> AlbumsScreen(onAlbumClick = onAlbumClick)
                        5 -> ArtistsScreen(onArtistClick = onArtistClick)
                    }
                }
            }
        }
    }
}