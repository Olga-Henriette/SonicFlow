package com.sonicflow.app.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.formatDuration
import kotlin.math.roundToInt

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.KeyboardArrowDown, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {

            if (state.currentSong == null) {
                // Aucune chanson en lecture
                NoSongPlaying(modifier = Modifier.align(Alignment.Center))
            } else {
                // Afficher le player
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(32.dp))

                    // Artwork (pour l'instant un placeholder)
                    AlbumArtwork(
                        modifier = Modifier
                            .size(320.dp)
                            .weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Infos chanson
                    SongInfo(
                        title = state.currentSong?.title ?: "Unknown",
                        artist = state.currentSong?.artist ?: "Unknown Artist"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Barre de progression
                    ProgressBar(
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        onSeek = { position ->
                            viewModel.handleIntent(PlayerIntent.SeekTo(position))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contrôles principaux
                    PlayerControls(
                        isPlaying = state.isPlaying,
                        onPlayPause = {
                            viewModel.handleIntent(PlayerIntent.PlayPause)
                        },
                        onNext = {
                            viewModel.handleIntent(PlayerIntent.Next)
                        },
                        onPrevious = {
                            viewModel.handleIntent(PlayerIntent.Previous)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Contrôles secondaires
                    SecondaryControls(
                        isShuffled = state.isShuffled,
                        repeatMode = state.repeatMode,
                        onShuffleToggle = {
                            viewModel.handleIntent(PlayerIntent.ToggleShuffle)
                        },
                        onRepeatToggle = {
                            viewModel.handleIntent(PlayerIntent.ToggleRepeat)
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun NoSongPlaying(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No song playing",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlbumArtwork(modifier: Modifier = Modifier) {
    // Pour l'instant, un placeholder
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SongInfo(
    title: String,
    artist: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Slider
        Slider(
            value = if (duration > 0) {
                (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            onValueChange = { progress ->
                val newPosition = (progress * duration).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentPosition.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(40.dp)
            )
        }

        // Play/Pause
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(80.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(48.dp)
            )
        }

        // Next
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun SecondaryControls(
    isShuffled: Boolean,
    repeatMode: RepeatMode,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // Favorite (placeholder)
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite"
            )
        }

        // Repeat
        IconButton(onClick = onRepeatToggle) {
            Icon(
                imageVector = when (repeatMode) {
                    RepeatMode.OFF -> Icons.Default.Repeat
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    RepeatMode.ALL -> Icons.Default.Repeat
                },
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        // Queue (placeholder)
        IconButton(onClick = { /* TODO */ }) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Queue"
            )
        }
    }
}