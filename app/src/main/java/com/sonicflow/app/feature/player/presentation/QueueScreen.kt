package com.sonicflow.app.feature.player.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.formatDuration
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.AlbumArtImage
import com.sonicflow.app.core.ui.components.ConfirmationDialog

@Composable
fun QueueScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Queue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.queue.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear queue",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.queue.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Queue is empty",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                QueueHeader(
                    currentIndex = state.currentIndex,
                    totalSongs = state.queue.size
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = state.queue,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        QueueSongItem(
                            song = song,
                            isCurrentSong = index == state.currentIndex,
                            position = index + 1,
                            onSongClick = {
                                viewModel.handleIntent(
                                    PlayerIntent.PlayQueue(state.queue, index)
                                )
                            },
                            onRemoveClick = {
                                viewModel.handleIntent(
                                    PlayerIntent.RemoveFromQueue(index)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ConfirmationDialog(
            title = "Clear Queue",
            message = "Remove all ${state.queue.size} songs from the queue? This will stop playback.",
            icon = Icons.Outlined.DeleteSweep,
            confirmText = "Clear All",
            isDestructive = true,
            onConfirm = {
                viewModel.handleIntent(PlayerIntent.ClearQueue)
                showClearDialog = false
                onNavigateBack()
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

@Composable
fun QueueHeader(
    currentIndex: Int,
    totalSongs: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentIndex + 1} of $totalSongs",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QueueSongItem(
    song: Song,
    isCurrentSong: Boolean,
    position: Int,
    onSongClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.width(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrentSong) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = position.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentSong) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        supportingContent = {
            Text(
                text = song.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentSong) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
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
                Text(
                    text = song.duration.formatDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remove from queue",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        modifier = modifier
            .clickable(onClick = onSongClick)
            .then(
                if (isCurrentSong) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                } else {
                    Modifier
                }
            )
    )
    HorizontalDivider()
}