package com.sonicflow.app.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.media3.common.PlaybackParameters
import com.sonicflow.app.core.player.controller.PlayerController
import com.sonicflow.app.core.player.service.CrossfadeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class PlaybackControlsState(
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 3 // secondes
)

@HiltViewModel
class PlaybackControlsViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val crossfadeManager: CrossfadeManager
) : ViewModel() {

    private val _state = MutableStateFlow(PlaybackControlsState())
    val state: StateFlow<PlaybackControlsState> = _state.asStateFlow()

    init {
        loadCurrentParameters()
    }

    private fun loadCurrentParameters() {
        try {
            val params = playerController.getPlaybackParameters()
            _state.update {
                it.copy(
                    speed = params.speed,
                    pitch = params.pitch
                )
            }
            Timber.d("Loaded playback parameters: speed=${params.speed}, pitch=${params.pitch}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load playback parameters")
        }
    }

    fun setSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    fun setPitch(pitch: Float) {
        playerController.setPitch(pitch)
        _state.update { it.copy(pitch = pitch) }
    }

    fun resetParameters() {
        playerController.resetPlaybackParameters()
        _state.update {
            it.copy(speed = 1.0f, pitch = 1.0f)
        }
    }

    fun toggleCrossfade() {
        val newState = !_state.value.crossfadeEnabled
        crossfadeManager.isEnabled = newState
        _state.update { it.copy(crossfadeEnabled = newState) }
        Timber.d("Crossfade toggled: $newState")
    }

    fun setCrossfadeDuration(duration: Int) {
        crossfadeManager.duration = duration
        _state.update { it.copy(crossfadeDuration = duration) }
        Timber.d("Crossfade duration set to: ${duration}s")
    }

    // Presets de vitesse courants
    val speedPresets = listOf(
        0.25f to "0.25x",
        0.5f to "0.5x",
        0.75f to "0.75x",
        1.0f to "Normal",
        1.25f to "1.25x",
        1.5f to "1.5x",
        1.75f to "1.75x",
        2.0f to "2x",
        2.5f to "2.5x",
        3.0f to "3x"
    )

    // Presets de pitch
    val pitchPresets = listOf(
        0.5f to "-12",
        0.75f to "-6",
        1.0f to "0",
        1.25f to "+6",
        1.5f to "+12"
    )
}