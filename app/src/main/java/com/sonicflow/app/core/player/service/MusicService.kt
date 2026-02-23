package com.sonicflow.app.core.player.service

import android.content.Intent
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonicflow.app.core.player.controller.PlayerController
import com.sonicflow.app.core.player.notification.MusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var playerController: PlayerController

    @Inject
    lateinit var notificationManager: MusicNotificationManager

    private var mediaSession: MediaSession? = null

    // Listener pour mettre à jour la notification
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            updateNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()

        val player = playerController.getExoPlayer()

        // Ajouter le listener
        player.addListener(playerListener)

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val connectionResult = super.onConnect(session, controller)
                    return MediaSession.ConnectionResult.accept(
                        connectionResult.availableSessionCommands,
                        connectionResult.availablePlayerCommands
                    )
                }
            })
            .build()

        Timber.d("MusicService created with MediaSession")
    }

    // Mettre à jour la notification avec hasNext/hasPrevious
    private fun updateNotification() {
        val session = mediaSession ?: return
        val player = session.player

        val hasNext = when {
            player.mediaItemCount == 0 -> false
            player.repeatMode == Player.REPEAT_MODE_ALL -> true
            player.repeatMode == Player.REPEAT_MODE_ONE -> true
            else -> player.currentMediaItemIndex < player.mediaItemCount - 1
        }

        val hasPrevious = when {
            player.mediaItemCount == 0 -> false
            player.repeatMode == Player.REPEAT_MODE_ALL -> true
            player.repeatMode == Player.REPEAT_MODE_ONE -> true
            player.currentPosition > 3000 -> true // Si > 3s, restart song
            else -> player.currentMediaItemIndex > 0
        }

        notificationManager.updateNotification(session, hasNext, hasPrevious)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.removeListener(playerListener)
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
        Timber.d("MusicService destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // La notification sera gérée automatiquement par Media3
        }
        return super.onStartCommand(intent, flags, startId)
    }
}