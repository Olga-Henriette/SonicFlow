package com.sonicflow.app.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BedtimeOff
import androidx.compose.material3.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.sonicflow.app.feature.playlist.presentation.PlaylistViewModel
import com.sonicflow.app.feature.playlist.components.AddToPlaylistDialog
import com.sonicflow.app.feature.player.components.SleepTimerDialog
import com.sonicflow.app.core.common.AlbumPalette
import com.sonicflow.app.core.common.PaletteExtractor
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.common.formatDuration
import com.sonicflow.app.core.ui.components.AlbumArtImage
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch
import coil3.imageLoader
import timber.log.Timber

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    albumPalette: AlbumPalette? = null,
    onAlbumPaletteChange: (AlbumPalette?) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onQueueClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val sleepTimerMinutes by viewModel.sleepTimerMinutes.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var localPalette by remember { mutableStateOf<AlbumPalette?>(null) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = (localPalette ?: albumPalette)?.background
            ?: MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 600),
        label = "background color"
    )

    LaunchedEffect(state.currentSong?.albumId) {
        state.currentSong?.albumId?.let { albumId ->
            scope.launch {
                val palette = PaletteExtractor.extractPalette(
                    context = context,
                    imageLoader = context.imageLoader,
                    albumId = albumId
                )
                localPalette = palette
                onAlbumPaletteChange(palette)
                Timber.d("🎨 Palette extracted: ${palette?.primary}")
            }
        }
    }

    LaunchedEffect(state.isShuffled) {
        if (state.isShuffled) {
            context.showToast("Shuffle on")
        }
    }

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
                            animatedBackgroundColor,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {

            if (state.currentSong == null) {
                NoSongPlaying(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(32.dp))

                    AlbumArtwork(
                        albumId = state.currentSong?.albumId ?: 0L,
                        onSwipeLeft = {
                            viewModel.handleIntent(PlayerIntent.Next)
                        },
                        onSwipeRight = {
                            viewModel.handleIntent(PlayerIntent.Previous)
                        },
                        modifier = Modifier
                            .size(320.dp)
                            .weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    SongInfo(
                        title = state.currentSong?.title ?: "Unknown",
                        artist = state.currentSong?.artist ?: "Unknown Artist"
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ProgressBar(
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        onSeek = { position ->
                            viewModel.handleIntent(PlayerIntent.SeekTo(position))
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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

                    SecondaryControls(
                        currentSongId = state.currentSong?.id,
                        currentSong = state.currentSong,
                        isFavorite = state.currentSong?.isFavorite ?: false,
                        isShuffled = state.isShuffled,
                        repeatMode = state.repeatMode,
                        queueSize = state.queue.size,
                        sleepTimerMinutes = sleepTimerMinutes,
                        onFavoriteToggle = {
                            state.currentSong?.let { song ->
                                viewModel.handleIntent(PlayerIntent.ToggleFavorite(song.id))
                            }
                        },
                        onShuffleToggle = {
                            viewModel.handleIntent(PlayerIntent.ToggleShuffle)
                        },
                        onRepeatToggle = {
                            viewModel.handleIntent(PlayerIntent.ToggleRepeat)
                        },
                        onQueueClick = onQueueClick,
                        onSleepTimerClick = {
                            showSleepTimerDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    // Dialog du sleep timer
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentTimerMinutes = sleepTimerMinutes,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { minutes ->
                viewModel.startSleepTimer(minutes)
                context.showToast("Sleep timer set for $minutes minutes")
            },
            onCancelTimer = {
                viewModel.cancelSleepTimer()
                context.showToast("Sleep timer cancelled")
            }
        )
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
fun AlbumArtwork(
    albumId: Long,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val animatedOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val swipeThreshold = 150.dp.value

    LaunchedEffect(offsetX) {
        if (!isDragging) {
            animatedOffsetX.animateTo(
                targetValue = offsetX,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDragEnd = {
                        isDragging = false

                        if (offsetX.absoluteValue > swipeThreshold) {
                            if (offsetX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }

                        scope.launch {
                            offsetX = 0f
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        scope.launch {
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount).coerceIn(-300f, 300f)
                        offsetX = newOffset
                        scope.launch {
                            animatedOffsetX.snapTo(newOffset)
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = animatedOffsetX.value
                rotationZ = (animatedOffsetX.value / 30f).coerceIn(-10f, 10f)
                alpha = 1f - (animatedOffsetX.value.absoluteValue / 600f).coerceIn(0f, 0.5f)
            }
    ) {
        AlbumArtImage(
            albumId = albumId,
            contentDescription = "Album artwork",
            modifier = Modifier.fillMaxSize(),
            size = 320.dp
        )

        if (isDragging && offsetX.absoluteValue > 50f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (offsetX > 0) {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = (offsetX / 300f).coerceIn(0f, 0.3f)
                            )
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(
                                alpha = (offsetX.absoluteValue / 300f).coerceIn(0f, 0.3f)
                            )
                        }
                    ),
                contentAlignment = if (offsetX > 0) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = if (offsetX > 0) {
                        Icons.Default.SkipPrevious
                    } else {
                        Icons.Default.SkipNext
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp),
                    tint = Color.White.copy(
                        alpha = (offsetX.absoluteValue / 150f).coerceIn(0f, 1f)
                    )
                )
            }
        }
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
    currentSongId: Long?,
    currentSong: Song?,
    isFavorite: Boolean,
    isShuffled: Boolean,
    repeatMode: RepeatMode,
    queueSize: Int = 0,
    sleepTimerMinutes: Int? = null,
    onFavoriteToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onQueueClick: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val playlistViewModel: PlaylistViewModel = hiltViewModel()
    val playlists by playlistViewModel.playlists.collectAsState()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = if (isShuffled) {
                    Modifier.size(28.dp)
                } else {
                    Modifier.size(24.dp)
                }
            )
        }

        // Sleep Timer
        BadgedBox(
            badge = {
                if (sleepTimerMinutes != null) {
                    Badge {
                        Text("${sleepTimerMinutes}m")
                    }
                }
            }
        ) {
            IconButton(onClick = onSleepTimerClick) {
                Icon(
                    imageVector = if (sleepTimerMinutes != null) {
                        Icons.Default.BedtimeOff
                    } else {
                        Icons.Default.Bedtime
                    },
                    contentDescription = "Sleep Timer",
                    tint = if (sleepTimerMinutes != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        IconButton(
            onClick = onFavoriteToggle,
            enabled = currentSongId != null
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

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

        IconButton(
            onClick = { showAddToPlaylist = true },
            enabled = currentSongId != null
        ) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = "Add to playlist"
            )
        }

        BadgedBox(
            badge = {
                if (queueSize > 0) {
                    Badge {
                        Text(queueSize.toString())
                    }
                }
            }
        ) {
            IconButton(onClick = onQueueClick) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Queue"
                )
            }
        }
    }

    if (showAddToPlaylist && currentSong != null) {
        AddToPlaylistDialog(
            song = currentSong,
            playlists = playlists,
            onDismiss = { showAddToPlaylist = false },
            onPlaylistSelected = { playlist ->
                playerViewModel.handleIntent(
                    PlayerIntent.AddToPlaylist(playlist.id, currentSong.id)
                )
                context.showToast("Added to ${playlist.name}")
                showAddToPlaylist = false
            },
            onCreateNewPlaylist = {
                showAddToPlaylist = false
            }
        )
    }
}