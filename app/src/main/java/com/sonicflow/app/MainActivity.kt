package com.sonicflow.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.sonicflow.app.core.domain.model.Album
import com.sonicflow.app.core.domain.model.Artist
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.player.service.MusicService
import com.sonicflow.app.core.ui.theme.SonicFlowTheme
import com.sonicflow.app.feature.library.presentation.*
import com.sonicflow.app.feature.player.presentation.*
import com.sonicflow.app.feature.playlist.presentation.PlaylistDetailScreen
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        setupUI()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge activé
        WindowCompat.setDecorFitsSystemWindows(window, false)

        startMusicService()
        initializeMediaController()
        requestAudioPermission()
    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Timber.d("MusicService started")
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            this,
            ComponentName(this, MusicService::class.java)
        )

        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            { Timber.d("MediaController connected") },
            MoreExecutors.directExecutor()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }

    private fun setupUI() {
        setContent {
            SonicFlowTheme {

                val view = LocalView.current
                val colorScheme = MaterialTheme.colorScheme

                // Gestion automatique couleur icônes status bar
                SideEffect {
                    val window = (view.context as ComponentActivity).window
                    val controller = WindowCompat.getInsetsController(window, view)

                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT

                    val isLight = colorScheme.background.luminance() > 0.5f
                    controller.isAppearanceLightStatusBars = isLight
                    controller.isAppearanceLightNavigationBars = isLight
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen {
    data object Library : Screen()
    data object Player : Screen()
    data class PlaylistDetail(val playlist: Playlist) : Screen()
    data class AlbumDetail(val album: Album) : Screen()
    data class ArtistDetail(val artist: Artist) : Screen()
    data object Queue : Screen()
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }
    var selectedLibraryTab by remember { mutableIntStateOf(0) }
    val playerViewModel: PlayerViewModel = hiltViewModel()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            // Slide horizontal pour Player
            if (targetState is Screen.Player || initialState is Screen.Player) {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(300)
                ) togetherWith slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(300)
                )
            } else {
                // Fade pour les autres transitions
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        },
        label = "screen transition"
    ) { screen ->
        when (screen) {
            Screen.Library -> {
                LibraryScreen(
                    playerViewModel = playerViewModel,
                    initialTab = selectedLibraryTab,
                    onTabChanged = { newTab -> selectedLibraryTab = newTab },
                    onSongClick = { song, allSongs ->
                        val startIndex = allSongs.indexOf(song)
                        playerViewModel.handleIntent(
                            PlayerIntent.PlayQueue(
                                songs = allSongs,
                                startIndex = startIndex
                            )
                        )
                        currentScreen = Screen.Player
                    },
                    onPlaylistClick = { playlist ->
                        selectedLibraryTab = 3 // Playlists tab
                        currentScreen = Screen.PlaylistDetail(playlist)
                    },
                    onAlbumClick = { album ->
                        selectedLibraryTab = 4 // Albums tab
                        currentScreen = Screen.AlbumDetail(album)
                    },
                    onArtistClick = { artist ->
                        selectedLibraryTab = 5 // Artists tab
                        currentScreen = Screen.ArtistDetail(artist)
                    },
                    onMiniPlayerClick = {
                        currentScreen = Screen.Player
                    }
                )
            }
            Screen.Player -> {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Library
                    },
                    onQueueClick = {
                        currentScreen = Screen.Queue
                    }
                )
            }
            is Screen.PlaylistDetail -> {
                PlaylistDetailScreen(
                    playlist = screen.playlist,
                    playerViewModel = playerViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Library
                    }
                )
            }
            is Screen.AlbumDetail -> {
                AlbumDetailScreen(
                    album = screen.album,
                    playerViewModel = playerViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Library
                    }
                )
            }
            is Screen.ArtistDetail -> {
                ArtistDetailScreen(
                    artist = screen.artist,
                    playerViewModel = playerViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Library
                    }
                )
            }
            Screen.Queue -> {
                QueueScreen(
                    viewModel = playerViewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Player
                    }
                )
            }
        }
    }
}