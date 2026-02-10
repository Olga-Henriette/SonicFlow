package com.sonicflow.app.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.usecase.AddSongToPlaylistUseCase
import com.sonicflow.app.core.domain.usecase.IncrementPlayCountUseCase
import com.sonicflow.app.core.domain.usecase.RemoveSongFromPlaylistUseCase // ← VÉRIFIER CETTE LIGNE
import com.sonicflow.app.core.domain.usecase.ToggleFavoriteUseCase
import com.sonicflow.app.core.player.controller.PlayerController
import com.sonicflow.app.core.player.service.SleepTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel du lecteur (MVI pattern)
 * - Gère l'état unique PlayerState
 * - Reçoit les intentions PlayerIntent
 * - Communique avec PlayerController
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val incrementPlayCountUseCase: IncrementPlayCountUseCase,
    private val sleepTimerManager: SleepTimerManager
) : ViewModel() {

    // État unique du player
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // Job pour mettre à jour la position
    private var positionUpdateJob: Job? = null

    private var _originalQueue: List<Song> = emptyList()
    private var _originalIndex: Int = 0

    init {
        // Observer les changements du PlayerController
        observePlayerController()
        // Démarrer la mise à jour de la position
        startPositionUpdates()

        // Écouter la fin de chanson
        playerController.onSongEnded = {
            handleIntent(PlayerIntent.Next)
        }

        // Écouter la fin du timer
        sleepTimerManager.onTimerFinished = {
            playerController.pause()
        }

        playerController.onMediaItemTransition = { newIndex ->
            val currentState = _state.value
            if (newIndex < currentState.queue.size) {
                _state.update {
                    it.copy(
                        currentIndex = newIndex,
                        currentSong = currentState.queue[newIndex]
                    )
                }
            }
        }
    }

    // Flow pour le timer
    val sleepTimerMinutes: StateFlow<Int?> = sleepTimerManager.remainingMinutes

    // Fonctions pour gérer le timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerManager.startTimer(minutes)
        Timber.d("Sleep timer started: $minutes minutes")
    }

    fun cancelSleepTimer() {
        sleepTimerManager.cancelTimer()
    }

    /**
     * Point d'entrée unique pour les intentions
     */
    fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlaySong -> playSong(intent.song)
            is PlayerIntent.PlayQueue -> playQueue(intent.songs, intent.startIndex)
            is PlayerIntent.PlayPause -> playerController.togglePlayPause()
            is PlayerIntent.Play -> playerController.play()
            is PlayerIntent.Pause -> playerController.pause()
            is PlayerIntent.Next -> playNext()
            is PlayerIntent.Previous -> playPrevious()
            is PlayerIntent.SeekTo -> playerController.seekTo(intent.position)
            is PlayerIntent.AddToQueue -> addToQueue(intent.song)
            is PlayerIntent.RemoveFromQueue -> removeFromQueue(intent.index)
            is PlayerIntent.ClearQueue -> clearQueue()
            is PlayerIntent.ToggleShuffle -> toggleShuffle()
            is PlayerIntent.ToggleRepeat -> toggleRepeat()
            is PlayerIntent.ToggleFavorite -> toggleFavorite(intent.songId)
            is PlayerIntent.AddToPlaylist -> addToPlaylist(intent.playlistId, intent.songId)
            is PlayerIntent.RemoveFromPlaylist -> removeFromPlaylist(intent.playlistId, intent.songId)
        }
    }

    /**
     * Observer les changements du PlayerController
     */
    private fun observePlayerController() {
        viewModelScope.launch {
            playerController.isPlaying.collect { isPlaying ->
                _state.update { it.copy(isPlaying = isPlaying) }
            }
        }

        viewModelScope.launch {
            playerController.currentSong.collect { song ->
                _state.update { it.copy(currentSong = song) }
            }
        }

        viewModelScope.launch {
            playerController.duration.collect { duration ->
                _state.update { it.copy(duration = duration) }
            }
        }
    }

    /**
     * Mettre à jour la position toutes les 100ms
     */
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                if (_state.value.isPlaying) {
                    playerController.updatePosition()
                    _state.update {
                        it.copy(currentPosition = playerController.currentPosition.value)
                    }
                }
                delay(100)
            }
        }
    }

    /**
     * Jouer une chanson
     */
    private fun playSong(song: Song) {
        Timber.d("Playing song: ${song.title}")

        playerController.setQueue(listOf(song), 0)

        _state.update {
            it.copy(
                currentSong = song,
                queue = listOf(song),
                currentIndex = 0
            )
        }
        // Incrémenter le compteur de lecture
        viewModelScope.launch {
            incrementPlayCountUseCase(song.id)
        }
    }

    /**
     * Jouer une queue de chansons
     */
    private fun playQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return

        val index = startIndex.coerceIn(0, songs.lastIndex)

        playerController.setQueue(songs, index)

        _state.update {
            it.copy(
                queue = songs,
                currentIndex = index,
                currentSong = songs[index]
            )
        }

        viewModelScope.launch {
            incrementPlayCountUseCase(songs[index].id)
        }
    }

    /**
     * Chanson suivante
     */
    private fun playNext() {
        val currentState = _state.value
        if (currentState.queue.isEmpty()) return

        val nextIndex = when (currentState.repeatMode) {
            RepeatMode.ONE -> currentState.currentIndex
            RepeatMode.ALL -> (currentState.currentIndex + 1) % currentState.queue.size
            RepeatMode.OFF -> {
                if (currentState.currentIndex < currentState.queue.lastIndex) {
                    currentState.currentIndex + 1
                } else {
                    return // Fin de la queue
                }
            }
        }

        val nextSong = currentState.queue[nextIndex]
        playerController.playSong(nextSong)
        _state.update {
            it.copy(
                currentIndex = nextIndex,
                currentSong = nextSong
            )
        }
    }

    /**
     * Chanson précédente
     */
    private fun playPrevious() {
        val currentState = _state.value
        if (currentState.queue.isEmpty()) return

        // Si on est au-delà de 3 secondes, recommencer la chanson
        if (currentState.currentPosition > 3000) {
            playerController.seekTo(0)
            return
        }

        val prevIndex = when (currentState.repeatMode) {
            RepeatMode.ONE -> currentState.currentIndex
            RepeatMode.ALL -> {
                if (currentState.currentIndex > 0) {
                    currentState.currentIndex - 1
                } else {
                    currentState.queue.lastIndex
                }
            }
            RepeatMode.OFF -> {
                if (currentState.currentIndex > 0) {
                    currentState.currentIndex - 1
                } else {
                    return
                }
            }
        }

        val prevSong = currentState.queue[prevIndex]
        playerController.playSong(prevSong)
        _state.update {
            it.copy(
                currentIndex = prevIndex,
                currentSong = prevSong
            )
        }
    }

    private fun addToQueue(song: Song) {
        _state.update {
            it.copy(queue = it.queue + song)
        }
    }

    private fun removeFromQueue(index: Int) {
        _state.update {
            it.copy(queue = it.queue.filterIndexed { i, _ -> i != index })
        }
    }

    private fun clearQueue() {
        playerController.pause()
        playerController.release()

        _state.update {
            it.copy(
                queue = emptyList(),
                currentSong = null,
                currentIndex = 0,
                isPlaying = false,
                isShuffled = false
            )
        }

        _originalQueue = emptyList()
        _originalIndex = 0

        Timber.d("Queue cleared")
    }
    private fun toggleShuffle() {
        val currentState = _state.value
        val newShuffleState = !currentState.isShuffled

        if (newShuffleState) {
            // Activer shuffle : mélanger la queue
            shuffleQueue()
        } else {
            // Désactiver shuffle : restaurer l'ordre original
            unshuffleQueue()
        }

        _state.update { it.copy(isShuffled = newShuffleState) }
    }
    private fun toggleRepeat() {
        val newMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _state.update { it.copy(repeatMode = newMode) }
    }
    private fun shuffleQueue() {
        val currentState = _state.value
        if (currentState.queue.isEmpty()) return

        val currentSong = currentState.queue.getOrNull(currentState.currentIndex)
        val otherSongs = currentState.queue.toMutableList().apply {
            removeAt(currentState.currentIndex)
        }.shuffled()

        val newQueue = if (currentSong != null) {
            listOf(currentSong) + otherSongs
        } else {
            otherSongs
        }

        // Sauvegarder l'ordre original pour pouvoir le restaurer
        _originalQueue = currentState.queue
        _originalIndex = currentState.currentIndex

        _state.update {
            it.copy(
                queue = newQueue,
                currentIndex = 0 // La chanson actuelle est maintenant en position 0
            )
        }

        // Mettre à jour ExoPlayer avec la nouvelle queue
        playerController.setQueue(newQueue, 0)

        Timber.d("Queue shuffled: ${newQueue.size} songs")
    }
    private fun unshuffleQueue() {
        if (_originalQueue.isEmpty()) return

        val currentState = _state.value
        val currentSong = currentState.currentSong

        // Trouver l'index de la chanson actuelle dans la queue originale
        val newIndex = _originalQueue.indexOfFirst { it.id == currentSong?.id }
            .takeIf { it >= 0 } ?: _originalIndex

        _state.update {
            it.copy(
                queue = _originalQueue,
                currentIndex = newIndex
            )
        }

        // Mettre à jour ExoPlayer
        playerController.setQueue(_originalQueue, newIndex)

        Timber.d("Queue unshuffled, back to original order")

        // Réinitialiser
        _originalQueue = emptyList()
        _originalIndex = 0
    }
    private fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(songId)

            // Rafraîchir l'état de la chanson
            _state.update { currentState ->
                currentState.copy(
                    currentSong = currentState.currentSong?.copy(
                        isFavorite = !currentState.currentSong.isFavorite
                    )
                )
            }
        }
    }

    private fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            try {
                addSongToPlaylistUseCase(playlistId, songId)
                Timber.d("Song $songId added to playlist $playlistId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to add song to playlist")
            }
        }
    }

    private fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            try {
                removeSongFromPlaylistUseCase(playlistId, songId)
                Timber.d("Song $songId removed from playlist $playlistId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove song from playlist")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
    }
}