package com.sonicflow.app.feature.player.presentation

import com.sonicflow.app.core.domain.model.Song

/**
 * État unique du lecteur (MVI pattern)
 * - Représente TOUT l'état du player à un instant T
 * - Immutable (data class)
 */
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isShuffled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isLoading: Boolean = false
)

/**
 * Modes de répétition
 */
enum class RepeatMode {
    OFF,      // Pas de répétition
    ONE,      // Répéter la chanson actuelle
    ALL       // Répéter toute la queue
}