// feature/player/presentation/EqualizerViewModel.kt
package com.sonicflow.app.feature.player.presentation

import androidx.lifecycle.ViewModel
import com.sonicflow.app.core.player.controller.EqualizerController
import com.sonicflow.app.core.player.controller.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

data class EqualizerState(
    val isEnabled: Boolean = false,
    val selectedPreset: String = "Normal",
    val bandLevels: List<Short> = listOf(0, 0, 0, 0, 0),
    val bassBoostStrength: Short = 0,
    val numberOfBands: Int = 5,
    val bandLevelRange: Pair<Short, Short> = Pair(-1500, 1500)
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    private val equalizerController = playerController.equalizerController

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    init {
        loadCurrentSettings()
    }

    private fun loadCurrentSettings() {
        try {
            val numberOfBands = equalizerController.getNumberOfBands().toInt()
            val bandLevelRange = equalizerController.getBandLevelRange()
            val bandLevels = mutableListOf<Short>()

            for (i in 0 until numberOfBands) {
                bandLevels.add(equalizerController.getBandLevel(i.toShort()))
            }

            _state.update {
                it.copy(
                    numberOfBands = numberOfBands,
                    bandLevelRange = bandLevelRange,
                    bandLevels = bandLevels,
                    bassBoostStrength = equalizerController.getBassBoostStrength()
                )
            }

            Timber.d("Equalizer settings loaded: $numberOfBands bands")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load equalizer settings")
        }
    }

    fun toggleEnabled() {
        val newState = !_state.value.isEnabled
        equalizerController.setEnabled(newState)
        _state.update { it.copy(isEnabled = newState) }
        Timber.d("Equalizer toggled: $newState")
    }

    fun setBandLevel(band: Int, level: Short) {
        equalizerController.setBandLevel(band.toShort(), level)

        val newBandLevels = _state.value.bandLevels.toMutableList()
        newBandLevels[band] = level

        _state.update {
            it.copy(
                bandLevels = newBandLevels,
                selectedPreset = "Custom"
            )
        }
    }

    fun applyPreset(preset: EqualizerController.EqualizerPreset) {
        equalizerController.applyPreset(preset)

        val numberOfBands = _state.value.numberOfBands
        val newBandLevels = mutableListOf<Short>()

        for (i in 0 until numberOfBands) {
            newBandLevels.add(preset.bandLevels.getOrNull(i) ?: 0)
        }

        _state.update {
            it.copy(
                selectedPreset = preset.name,
                bandLevels = newBandLevels,
                bassBoostStrength = preset.bassBoostStrength
            )
        }

        Timber.d("Preset applied: ${preset.name}")
    }

    fun setBassBoost(strength: Short) {
        equalizerController.setBassBoostStrength(strength)
        _state.update {
            it.copy(
                bassBoostStrength = strength,
                selectedPreset = if (it.selectedPreset != "Custom") "Custom" else it.selectedPreset
            )
        }
    }

    fun reset() {
        equalizerController.reset()
        loadCurrentSettings()
        _state.update { it.copy(selectedPreset = "Normal") }
    }

    fun getBandFrequency(band: Int): String {
        val freq = equalizerController.getCenterFreq(band.toShort())
        return when {
            freq < 1000 -> "${freq} Hz"
            else -> "${freq / 1000} kHz"
        }
    }
}