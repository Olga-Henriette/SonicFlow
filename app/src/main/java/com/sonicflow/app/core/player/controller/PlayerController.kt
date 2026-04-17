package com.sonicflow.app.core.player.controller

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
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

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    val equalizerController: EqualizerController
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    var onSongEnded: (() -> Unit)? = null
    var onMediaItemTransition: ((Long) -> Unit)? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _duration.value = exoPlayer.duration.coerceAtLeast(0)
            } else if (playbackState == Player.STATE_ENDED) {
                onSongEnded?.invoke()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                val songId = item.mediaId.toLongOrNull() ?: return
                onMediaItemTransition?.invoke(songId)
            }
        }
    }

    private fun findSongInCurrentQueue(mediaId: String): Song? {
        return null
    }

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(playerListener)
        repeatMode = Player.REPEAT_MODE_OFF
    }

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    equalizerController.initialize(exoPlayer)
                }
            }
        })
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> exoPlayer.volume = 0.2f
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer.volume = 1.0f
                play()
            }
        }
    }

    /**
     * Gère la requête de focus audio de manière sécurisée selon la version d'Android
     */
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusOreo()
        } else {
            requestAudioFocusLegacy()
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    private fun requestAudioFocusLegacy(): Boolean {
        return audioManager.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusOreo(): Boolean {
        val playbackAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(playbackAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()

        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    fun playSong(song: Song) {
        if (!requestAudioFocus()) return

        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _currentSong.value = song
    }

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

    fun setPlaybackSpeed(speed: Float) {
        val constrainedSpeed = speed.coerceIn(0.25f, 3.0f)
        exoPlayer.playbackParameters = PlaybackParameters(constrainedSpeed, exoPlayer.playbackParameters.pitch)
    }

    fun setPitch(pitch: Float) {
        try {
            val constrainedPitch = pitch.coerceIn(0.25f, 3.0f)
            val currentSpeed = exoPlayer.playbackParameters.speed

            exoPlayer.playbackParameters = PlaybackParameters(currentSpeed, constrainedPitch)
            Timber.d("Pitch set to: ${constrainedPitch}x")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set pitch")
        }
    }

    /**
     * Obtenir les paramètres de lecture actuels
     */
    fun getPlaybackParameters(): PlaybackParameters {
        return exoPlayer.playbackParameters
    }

    /**
     * Réinitialiser pitch et speed
     */
    fun resetPlaybackParameters() {
        exoPlayer.playbackParameters = PlaybackParameters.DEFAULT
        Timber.d("Playback parameters reset")
    }

    /**
     * Play / Pause
     */
    fun togglePlayPause() {
        if (exoPlayer.isPlaying) pause() else play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun play() {
        if (requestAudioFocus()) {
            exoPlayer.play()
        }
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
        equalizerController.release()
        Timber.d("Player released")
    }

    /**
     * Récupérer l'ExoPlayer (pour MediaSession)
     */
    fun getExoPlayer(): ExoPlayer = exoPlayer

    fun updatePosition() {
        if (exoPlayer.isPlaying) {
            _currentPosition.value = exoPlayer.currentPosition.coerceAtLeast(0)
        }
    }

    fun next() = if (exoPlayer.hasNextMediaItem()) exoPlayer.seekToNextMediaItem() else Unit

    fun previous() = if (exoPlayer.hasPreviousMediaItem()) exoPlayer.seekToPreviousMediaItem() else Unit
}