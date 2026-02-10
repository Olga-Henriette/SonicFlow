package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.usecase.GetMostPlayedUseCase
import com.sonicflow.app.core.domain.usecase.GetRecentlyPlayedUseCase
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel

@Composable
fun ForYouScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    getMostPlayedUseCase: GetMostPlayedUseCase,
    getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val allSongs by libraryViewModel.songs.collectAsState()

    // Recently played
    val recentlyPlayed by getRecentlyPlayedUseCase(10)
        .collectAsState(initial = emptyList())

    // Most played
    val mostPlayed by getMostPlayedUseCase(10)
        .collectAsState(initial = emptyList())

    // Recently added (derniers fichiers par date de modification)
    val recentlyAdded = remember(allSongs) {
        allSongs
            .sortedByDescending { it.dateAdded }
            .take(10)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "For You",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your personalized music",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Played",
                    icon = Icons.Default.History
                )
            }
            item {
                HorizontalSongList(
                    songs = recentlyPlayed,
                    playerViewModel = playerViewModel,
                    onSongClick = onSongClick
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Most Played
        if (mostPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Most Played",
                    icon = Icons.Default.TrendingUp
                )
            }
            item {
                HorizontalSongList(
                    songs = mostPlayed,
                    playerViewModel = playerViewModel,
                    onSongClick = onSongClick
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Recently Added
        if (recentlyAdded.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Added",
                    icon = Icons.Default.NewReleases
                )
            }
            item {
                HorizontalSongList(
                    songs = recentlyAdded,
                    playerViewModel = playerViewModel,
                    onSongClick = onSongClick
                )
            }
        }

        // Empty state
        if (recentlyPlayed.isEmpty() && mostPlayed.isEmpty() && recentlyAdded.isEmpty()) {
            item {
                EmptyForYouState()
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun HorizontalSongList(
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(songs) { song ->
            HorizontalSongCard(
                song = song,
                onClick = { onSongClick(song, songs) }
            )
        }
    }
}

@Composable
fun HorizontalSongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(160.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            com.sonicflow.app.core.ui.components.AlbumArtImage(
                albumId = song.albumId,
                contentDescription = song.title,
                size = 136.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyForYouState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start listening to music",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Your personalized recommendations will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}