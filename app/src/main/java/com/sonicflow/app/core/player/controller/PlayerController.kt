package com.sonicflow.app.core.player.controller

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sonicflow.app.core.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contrôleur du lecteur audio
 * - Abstraction d'ExoPlayer
 * - Gère la lecture, pause, skip, etc.
 * - Émet les changements d'état via Flow
 */
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // États du player
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    // Listener pour les événements ExoPlayer (AVANT exoPlayer)
    var onSongEnded: (() -> Unit)? = null
    var onMediaItemTransition: ((Int) -> Unit)? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            Timber.d("Player isPlaying: $isPlaying")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration.coerceAtLeast(0)
                    Timber.d("Player ready, duration: ${_duration.value}")
                }
                Player.STATE_ENDED -> {
                    Timber.d("Player ended - calling onSongEnded")
                    onSongEnded?.invoke()
                }
                Player.STATE_BUFFERING -> {
                    Timber.d("Player buffering")
                }
                Player.STATE_IDLE -> {
                    Timber.d("Player idle")
                }
            }
        }
    }

    // ExoPlayer instance (APRÈS playerListener)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        // Écouter les événements du player
        addListener(playerListener)

        repeatMode = Player.REPEAT_MODE_OFF
    }

    /**
     * Jouer une chanson
     */
    fun playSong(song: Song) {
        Timber.d("Playing song: ${song.title}")

        val mediaMetadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setDisplayTitle(song.title)
            .setSubtitle(song.artist)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(mediaMetadata)
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        _currentSong.value = song
    }

    /**
     * Définir une queue de chansons
     */
    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        Timber.d("Setting queue: ${songs.size} songs, start at $startIndex")

        val mediaItems = songs.map { song ->
            val mediaMetadata = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setDisplayTitle(song.title)
                .setSubtitle(song.artist)
                .build()

            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(mediaMetadata)
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    /**
     * Play / Pause
     */
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    /**
     * Pause
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Resume
     */
    fun play() {
        exoPlayer.play()
    }

    /**
     * Seek à une position
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    /**
     * Stop et libérer les ressources
     */
    fun release() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        Timber.d("Player released")
    }

    /**
     * Récupérer l'ExoPlayer (pour MediaSession)
     */
    fun getExoPlayer(): ExoPlayer = exoPlayer

    /**
     * Mettre à jour la position actuelle (appelé périodiquement)
     */
    fun updatePosition() {
        if (exoPlayer.isPlaying) {
            _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0)
        }
    }
}