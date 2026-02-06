package com.sonicflow.app

import android.Manifest
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
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.theme.SonicFlowTheme
import com.sonicflow.app.feature.library.presentation.LibraryScreen
import com.sonicflow.app.feature.player.presentation.PlayerIntent
import com.sonicflow.app.feature.player.presentation.PlayerScreen
import com.sonicflow.app.feature.player.presentation.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
        requestAudioPermission()
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
                onSongClick = { song ->
                    // Jouer la chanson et ouvrir le player
                    playerViewModel.handleIntent(PlayerIntent.PlaySong(song))
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