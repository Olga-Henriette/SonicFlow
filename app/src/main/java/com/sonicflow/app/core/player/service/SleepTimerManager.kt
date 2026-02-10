package com.sonicflow.app.core.player.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gère le sleep timer
 */
@Singleton
class SleepTimerManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _remainingMinutes = MutableStateFlow<Int?>(null)
    val remainingMinutes: StateFlow<Int?> = _remainingMinutes.asStateFlow()

    private var timerJob: Job? = null

    var onTimerFinished: (() -> Unit)? = null

    /**
     * Démarrer le timer
     */
    fun startTimer(minutes: Int) {
        cancelTimer()

        _remainingMinutes.value = minutes

        timerJob = scope.launch {
            var remaining = minutes

            while (remaining > 0 && isActive) {
                delay(60_000) // 1 minute
                remaining--
                _remainingMinutes.value = remaining
                Timber.d("Sleep timer: $remaining minutes remaining")
            }

            if (remaining == 0) {
                Timber.d("Sleep timer finished - stopping playback")
                onTimerFinished?.invoke()
                _remainingMinutes.value = null
            }
        }
    }

    /**
     * Annuler le timer
     */
    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _remainingMinutes.value = null
        Timber.d("Sleep timer cancelled")
    }

    /**
     * Vérifier si un timer est actif
     */
    fun isTimerActive(): Boolean = _remainingMinutes.value != null
}