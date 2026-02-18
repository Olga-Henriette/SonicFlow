// core/player/controller/EqualizerController.kt
package com.sonicflow.app.core.player.controller

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import androidx.media3.common.Player
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contrôleur d'égaliseur audio professionnel
 * Gère l'égaliseur, bass boost et reverb
 */
@Singleton
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var presetReverb: PresetReverb? = null

    private var isEnabled = false

    // Présets personnalisés
    data class EqualizerPreset(
        val name: String,
        val bandLevels: List<Short>, // Niveaux pour chaque bande
        val bassBoostStrength: Short = 0,
        val reverbPreset: Short = PresetReverb.PRESET_NONE
    )

    companion object {
        // Présets prédéfinis
        val PRESET_NORMAL = EqualizerPreset(
            name = "Normal",
            bandLevels = listOf(0, 0, 0, 0, 0)
        )

        val PRESET_CLASSICAL = EqualizerPreset(
            name = "Classical",
            bandLevels = listOf(500, 300, -200, 400, 500),
            reverbPreset = PresetReverb.PRESET_LARGEHALL
        )

        val PRESET_DANCE = EqualizerPreset(
            name = "Dance",
            bandLevels = listOf(400, 700, 300, 0, 200),
            bassBoostStrength = 700
        )

        val PRESET_FLAT = EqualizerPreset(
            name = "Flat",
            bandLevels = listOf(0, 0, 0, 0, 0)
        )

        val PRESET_FOLK = EqualizerPreset(
            name = "Folk",
            bandLevels = listOf(300, 100, 0, 200, -100)
        )

        val PRESET_HEAVY_METAL = EqualizerPreset(
            name = "Heavy Metal",
            bandLevels = listOf(400, 200, 500, 400, 100),
            bassBoostStrength = 500
        )

        val PRESET_HIP_HOP = EqualizerPreset(
            name = "Hip Hop",
            bandLevels = listOf(500, 300, 0, 200, 300),
            bassBoostStrength = 800
        )

        val PRESET_JAZZ = EqualizerPreset(
            name = "Jazz",
            bandLevels = listOf(400, 200, 400, 300, 400),
            reverbPreset = PresetReverb.PRESET_SMALLROOM
        )

        val PRESET_POP = EqualizerPreset(
            name = "Pop",
            bandLevels = listOf(-100, 300, 500, 300, -200),
            bassBoostStrength = 300
        )

        val PRESET_ROCK = EqualizerPreset(
            name = "Rock",
            bandLevels = listOf(500, 300, -100, 100, 500),
            bassBoostStrength = 400
        )

        val ALL_PRESETS = listOf(
            PRESET_NORMAL,
            PRESET_CLASSICAL,
            PRESET_DANCE,
            PRESET_FLAT,
            PRESET_FOLK,
            PRESET_HEAVY_METAL,
            PRESET_HIP_HOP,
            PRESET_JAZZ,
            PRESET_POP,
            PRESET_ROCK
        )
    }

    /**
     * Initialiser l'égaliseur avec l'audio session du player
     */

    fun initialize(audioSessionId: Int) {
        try {
            // Créer l'égaliseur
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = false
            }

            // Créer le bass boost
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = false
            }

            // Créer le reverb
            presetReverb = PresetReverb(0, audioSessionId).apply {
                enabled = false
            }

            Timber.d("Equalizer initialized with ${equalizer?.numberOfBands} bands")
            logBandInfo()
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize equalizer")
        }
    }

    /**
     * Activer/Désactiver l'égaliseur
     */
    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            presetReverb?.enabled = enabled
            isEnabled = enabled
            Timber.d("Equalizer enabled: $enabled")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle equalizer")
        }
    }

    /**
     * Obtenir le nombre de bandes
     */
    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 5
    }

    /**
     * Obtenir la plage de niveau pour les bandes (min, max)
     */
    fun getBandLevelRange(): Pair<Short, Short> {
        return equalizer?.let {
            Pair(it.bandLevelRange[0], it.bandLevelRange[1])
        } ?: Pair(-1500, 1500)
    }

    /**
     * Obtenir la fréquence centrale d'une bande
     */
    fun getCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 0
    }

    /**
     * Définir le niveau d'une bande
     */
    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
            Timber.d("Band $band set to $level")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set band level")
        }
    }

    /**
     * Obtenir le niveau d'une bande
     */
    fun getBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }

    /**
     * Appliquer un preset
     */
    fun applyPreset(preset: EqualizerPreset) {
        try {
            val numberOfBands = getNumberOfBands()

            // Appliquer les niveaux de bandes
            for (i in 0 until numberOfBands.toInt()) {
                val level = preset.bandLevels.getOrNull(i) ?: 0
                setBandLevel(i.toShort(), level)
            }

            // Appliquer bass boost
            bassBoost?.setStrength(preset.bassBoostStrength)

            // Appliquer reverb
            if (preset.reverbPreset != PresetReverb.PRESET_NONE) {
                presetReverb?.preset = preset.reverbPreset
            }

            Timber.d("Applied preset: ${preset.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to apply preset")
        }
    }

    /**
     * Définir la force du bass boost (0-1000)
     */
    fun setBassBoostStrength(strength: Short) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000))
            Timber.d("Bass boost set to $strength")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set bass boost")
        }
    }

    /**
     * Obtenir la force du bass boost
     */
    fun getBassBoostStrength(): Short {
        return bassBoost?.roundedStrength ?: 0
    }

    /**
     * Logger les informations sur les bandes
     */
    private fun logBandInfo() {
        equalizer?.let { eq ->
            val numberOfBands = eq.numberOfBands
            Timber.d("Equalizer has $numberOfBands bands")

            for (i in 0 until numberOfBands) {
                val freq = eq.getCenterFreq(i.toShort())
                Timber.d("Band $i: ${freq / 1000} Hz")
            }

            val range = eq.bandLevelRange
            Timber.d("Band level range: ${range[0]} to ${range[1]}")
        }
    }

    /**
     * Sauvegarder les paramètres actuels
     */
    fun getCurrentSettings(): EqualizerPreset {
        val numberOfBands = getNumberOfBands()
        val bandLevels = mutableListOf<Short>()

        for (i in 0 until numberOfBands) {
            bandLevels.add(getBandLevel(i.toShort()))
        }

        return EqualizerPreset(
            name = "Custom",
            bandLevels = bandLevels,
            bassBoostStrength = getBassBoostStrength(),
            reverbPreset = presetReverb?.preset ?: PresetReverb.PRESET_NONE
        )
    }

    /**
     * Réinitialiser l'égaliseur
     */
    fun reset() {
        applyPreset(PRESET_NORMAL)
        setBassBoostStrength(0)
    }

    /**
     * Libérer les ressources
     */
    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            presetReverb?.release()
            equalizer = null
            bassBoost = null
            presetReverb = null
            Timber.d("Equalizer released")
        } catch (e: Exception) {
            Timber.e(e, "Failed to release equalizer")
        }
    }
}