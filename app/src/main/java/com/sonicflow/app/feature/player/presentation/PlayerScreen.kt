package com.sonicflow.app.feature.player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.imageLoader
import com.sonicflow.app.core.common.AlbumPalette
import com.sonicflow.app.core.common.PaletteExtractor
import com.sonicflow.app.core.common.formatDuration
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.CircularAlbumArt
import com.sonicflow.app.feature.player.components.SleepTimerDialog
import com.sonicflow.app.feature.player.components.WaveformVisualizer
import com.sonicflow.app.feature.player.components.LyricsView
import com.sonicflow.app.feature.playlist.components.AddToPlaylistDialog
import com.sonicflow.app.feature.playlist.presentation.PlaylistViewModel
import kotlinx.coroutines.launch
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
    var showLyrics by remember { mutableStateOf(false) }
    var showWaveform by remember { mutableStateOf(false) }

    // Extraction palette
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
            }
        }
    }

    val currentPalette = localPalette ?: albumPalette
    val primaryColor = currentPalette?.primary ?: MaterialTheme.colorScheme.primary
    val backgroundColor = currentPalette?.background ?: MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Onglets Music / Lyrics
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TabButton(
                            text = "Music",
                            selected = !showLyrics,
                            onClick = { showLyrics = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TabButton(
                            text = "Lyrics",
                            selected = showLyrics,
                            onClick = { showLyrics = true }
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.KeyboardArrowDown, "Back")
                    }
                },
                actions = {
                    // Queue en haut à droite
                    BadgedBox(
                        badge = {
                            if (state.queue.size > 0) {
                                Badge {
                                    Text(state.queue.size.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = onQueueClick) {
                            Icon(Icons.Default.QueueMusic, "Queue")
                        }
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
                            backgroundColor,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (state.currentSong == null) {
                NoSongPlaying(modifier = Modifier.align(Alignment.Center))
            } else {
                if (showLyrics) {
                    // Vue Lyrics (TODO)
                    LyricsView(
                        song = state.currentSong,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Vue Music
                    MusicView(
                        song = state.currentSong!!,
                        isPlaying = state.isPlaying,
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        isShuffled = state.isShuffled,
                        repeatMode = state.repeatMode,
                        isFavorite = state.currentSong?.isFavorite ?: false,
                        primaryColor = primaryColor,
                        sleepTimerMinutes = sleepTimerMinutes,
                        showWaveform = showWaveform,
                        onToggleWaveform = { showWaveform = !showWaveform },
                        onPlayPause = {
                            viewModel.handleIntent(PlayerIntent.PlayPause)
                        },
                        onNext = {
                            viewModel.handleIntent(PlayerIntent.Next)
                        },
                        onPrevious = {
                            viewModel.handleIntent(PlayerIntent.Previous)
                        },
                        onSeek = { position ->
                            viewModel.handleIntent(PlayerIntent.SeekTo(position))
                        },
                        onFavoriteToggle = {
                            viewModel.handleIntent(PlayerIntent.ToggleFavorite(state.currentSong!!.id))
                        },
                        onShuffleRepeatToggle = {
                            // Cycle: OFF → All → Shuffle All → Repeat One → OFF
                            when {
                                !state.isShuffled && state.repeatMode == RepeatMode.OFF -> {
                                    viewModel.handleIntent(PlayerIntent.ToggleRepeat) // → All
                                }
                                !state.isShuffled && state.repeatMode == RepeatMode.ALL -> {
                                    viewModel.handleIntent(PlayerIntent.ToggleShuffle) // → Shuffle All
                                }
                                state.isShuffled && state.repeatMode == RepeatMode.ALL -> {
                                    viewModel.handleIntent(PlayerIntent.ToggleRepeat) // → Repeat One
                                    viewModel.handleIntent(PlayerIntent.ToggleShuffle) // Désactiver shuffle
                                }
                                else -> {
                                    viewModel.handleIntent(PlayerIntent.ToggleRepeat) // → OFF
                                }
                            }
                        },
                        onSleepTimerClick = {
                            showSleepTimerDialog = true
                        }
                    )
                }
            }
        }
    }

    // Dialogs
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
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun MusicView(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffled: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    primaryColor: Color,
    sleepTimerMinutes: Int?,
    showWaveform: Boolean,
    onToggleWaveform: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    onShuffleRepeatToggle: () -> Unit,
    onSleepTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddToPlaylist by remember { mutableStateOf(false) }
    val playlistViewModel: PlaylistViewModel = hiltViewModel()
    val playlists by playlistViewModel.playlists.collectAsState()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Pochette circulaire OU Waveform
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (showWaveform) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WaveformVisualizer(
                        isPlaying = isPlaying,
                        primaryColor = primaryColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Petit artwork au centre
                    CircularAlbumArt(
                        albumId = song.albumId,
                        isPlaying = isPlaying,
                        primaryColor = primaryColor,
                        size = 180.dp
                    )
                }
            } else {
                CircularAlbumArt(
                    albumId = song.albumId,
                    isPlaying = isPlaying,
                    primaryColor = primaryColor,
                    size = 280.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Titre + Favoris
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bouton favoris
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boutons secondaires : Add to | Waveform | Sleep Timer | More
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Add to playlist
            IconButton(onClick = { showAddToPlaylist = true }) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to playlist",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Waveform
            IconButton(onClick = onToggleWaveform) {
                Icon(
                    imageVector = if (showWaveform) {
                        Icons.Default.Album
                    } else {
                        Icons.Outlined.GraphicEq
                    },
                    contentDescription = "Toggle Waveform",
                    tint = if (showWaveform) {
                        primaryColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
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
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerMinutes != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // More options
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Barre de progression élégante
        ModernProgressBar(
            currentPosition = currentPosition,
            duration = duration,
            primaryColor = primaryColor,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Contrôles : Shuffle/Repeat | Previous | Play/Pause | Next | Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle/Repeat combiné
            IconButton(
                onClick = onShuffleRepeatToggle,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when {
                        isShuffled && repeatMode == RepeatMode.ALL -> Icons.Default.Shuffle
                        repeatMode == RepeatMode.ONE -> Icons.Default.RepeatOne
                        repeatMode == RepeatMode.ALL -> Icons.Default.Repeat
                        else -> Icons.Outlined.Repeat
                    },
                    contentDescription = "Shuffle/Repeat",
                    tint = if (isShuffled || repeatMode != RepeatMode.OFF) {
                        primaryColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

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
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = primaryColor
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
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

            // Equalizer (TODO)
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Equalizer,
                    contentDescription = "Equalizer"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialog Add to Playlist
    if (showAddToPlaylist) {
        AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { showAddToPlaylist = false },
            onPlaylistSelected = { playlist ->
                playerViewModel.handleIntent(
                    PlayerIntent.AddToPlaylist(playlist.id, song.id)
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

@Composable
fun ModernProgressBar(
    currentPosition: Long,
    duration: Long,
    primaryColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Slider personnalisé
        Slider(
            value = if (duration > 0) {
                (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            onValueChange = { progress ->
                val newPosition = (progress * duration).toLong()
                onSeek(newPosition)
            },
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = primaryColor.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentPosition.formatDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration.formatDuration(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LyricsView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lyrics not available",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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