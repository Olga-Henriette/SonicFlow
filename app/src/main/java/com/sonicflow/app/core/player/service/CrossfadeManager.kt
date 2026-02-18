package com.sonicflow.app.core.player.service

import androidx.media3.common.Player
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire de crossfade entre les chansons
 * Gère le fondu enchaîné pour des transitions fluides
 */
@Singleton
class CrossfadeManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var crossfadeJob: Job? = null

    var isEnabled: Boolean = false
    var duration: Int = 3 // secondes

    /**
     * Démarrer le crossfade avant la fin de la chanson
     */
    fun startCrossfadeIfNeeded(player: Player, remainingMs: Long) {
        if (!isEnabled || remainingMs > duration * 1000L) return

        crossfadeJob?.cancel()
        crossfadeJob = scope.launch {
            try {
                val steps = 20
                val stepDuration = (duration * 1000L) / steps

                for (step in steps downTo 0) {
                    val volume = step.toFloat() / steps
                    player.volume = volume
                    delay(stepDuration)
                }

                // Passer à la chanson suivante
                player.volume = 1.0f

            } catch (e: Exception) {
                Timber.e(e, "Crossfade failed")
                player.volume = 1.0f
            }
        }
    }

    /**
     * Annuler le crossfade en cours
     */
    fun cancel() {
        crossfadeJob?.cancel()
    }

    fun release() {
        crossfadeJob?.cancel()
        scope.cancel()
    }
}