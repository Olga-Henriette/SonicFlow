package com.sonicflow.app.feature.library.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.usecase.ClearPlayHistoryUseCase
import com.sonicflow.app.core.domain.usecase.GetMostPlayedUseCase
import com.sonicflow.app.core.domain.usecase.GetRecentlyPlayedUseCase
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun ForYouScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    getMostPlayedUseCase: GetMostPlayedUseCase,
    getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    clearPlayHistoryUseCase: ClearPlayHistoryUseCase = hiltViewModel<ForYouViewModel>().clearPlayHistoryUseCase,
    onSongClick: (Song, List<Song>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val allSongs by libraryViewModel.songs.collectAsState()

    val recentlyPlayed by getRecentlyPlayedUseCase(10)
        .collectAsState(initial = emptyList())

    val mostPlayed by getMostPlayedUseCase(10)
        .collectAsState(initial = emptyList())

    val recentlyAdded = remember(allSongs) {
        allSongs
            .sortedByDescending { it.dateAdded }
            .take(10)
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var sectionToClear by remember { mutableStateOf<ClearSection?>(null) }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (recentlyPlayed.isNotEmpty() || mostPlayed.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            sectionToClear = ClearSection.ALL
                            showClearDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Played",
                    icon = Icons.Default.History,
                    onClearClick = {
                        sectionToClear = ClearSection.RECENTLY_PLAYED
                        showClearDialog = true
                    }
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
                    icon = Icons.Default.TrendingUp,
                    onClearClick = {
                        sectionToClear = ClearSection.MOST_PLAYED
                        showClearDialog = true
                    }
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
                    icon = Icons.Default.NewReleases,
                    onClearClick = null // Pas de clear pour Recently Added
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

    if (showClearDialog) {
        ClearHistoryDialog(
            section = sectionToClear,
            onConfirm = {

                scope.launch {
                    when (sectionToClear) {
                        ClearSection.ALL -> clearPlayHistoryUseCase.clearAll()
                        ClearSection.RECENTLY_PLAYED -> clearPlayHistoryUseCase.clearRecentlyPlayed()
                        ClearSection.MOST_PLAYED -> clearPlayHistoryUseCase.clearMostPlayed()
                        null -> {}
                    }
                }

                showClearDialog = false
                sectionToClear = null
            },
            onDismiss = {
                showClearDialog = false
                sectionToClear = null
            }
        )
    }
}

enum class ClearSection {
    ALL,
    RECENTLY_PLAYED,
    MOST_PLAYED
}

@Composable
fun ClearHistoryDialog(
    section: ClearSection?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (section == null) return

    val (title, message) = when (section) {
        ClearSection.ALL -> "Clear All History" to "Remove all play history? This will clear Recently Played and Most Played."
        ClearSection.RECENTLY_PLAYED -> "Clear Recently Played" to "Remove all songs from Recently Played?"
        ClearSection.MOST_PLAYED -> "Clear Most Played" to "Remove all songs from Most Played?"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClearClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
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

        if (onClearClick != null) {
            IconButton(
                onClick = onClearClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Clear,
                    contentDescription = "Clear section",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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