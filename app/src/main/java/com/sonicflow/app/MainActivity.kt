package com.sonicflow.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.player.service.MusicService
import com.sonicflow.app.core.ui.theme.SonicFlowTheme
import com.sonicflow.app.feature.library.presentation.LibraryScreen
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerScreen
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupUI()
        } else {
            setupUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Démarrer le service
        startMusicService()

        // Connecter au MediaController
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
            {
                Timber.d("MediaController connected")
            },
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }
    val playerViewModel: PlayerViewModel = hiltViewModel()

    when (currentScreen) {
        Screen.Library -> {
            LibraryScreen(
                playerViewModel = playerViewModel,
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
                }
            )
        }
    }
}

sealed class Screen {
    data object Library : Screen()
    data object Player : Screen()
}