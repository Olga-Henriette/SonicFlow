package com.sonicflow.app.feature.player.presentation

import com.sonicflow.app.core.domain.model.Song

/**
 * Intentions utilisateur pour le player (MVI pattern)
 * - Sealed interface = liste fermée d'actions possibles
 * - Chaque action peut avoir des données associées
 */
sealed interface PlayerIntent {

    // Lecture
    data class PlaySong(val song: Song) : PlayerIntent
    data class PlayQueue(val songs: List<Song>, val startIndex: Int = 0) : PlayerIntent
    data object PlayPause : PlayerIntent
    data object Play : PlayerIntent
    data object Pause : PlayerIntent

    // Navigation
    data object Next : PlayerIntent
    data object Previous : PlayerIntent
    data class SeekTo(val position: Long) : PlayerIntent

    // Queue
    data class AddToQueue(val song: Song) : PlayerIntent
    data class RemoveFromQueue(val index: Int) : PlayerIntent
    data object ClearQueue : PlayerIntent

    // Options
    data object ToggleShuffle : PlayerIntent
    data object ToggleRepeat : PlayerIntent
    data class ToggleFavorite(val songId: Long) : PlayerIntent

    data class AddToPlaylist(val playlistId: Long, val songId: Long) : PlayerIntent

}