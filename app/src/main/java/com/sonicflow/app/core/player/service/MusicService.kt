package com.sonicflow.app.core.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonicflow.app.MainActivity
import com.sonicflow.app.core.player.controller.PlayerController
import com.sonicflow.app.core.player.notification.MusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Service de lecture audio en arrière-plan
 * - Survit même si l'app est fermée
 * - Gère MediaSession (contrôles lockscreen, Bluetooth, etc.)
 */
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var playerController: PlayerController

    @Inject
    lateinit var notificationManager: MusicNotificationManager

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // Créer la MediaSession
        mediaSession = MediaSession.Builder(this, playerController.getExoPlayer())
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

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // Arrêter le service si aucune lecture en cours
        val player = mediaSession?.player
        if (player?.playWhenReady == false) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
        Timber.d("MusicService destroyed")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Démarrer en foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // La notification sera gérée automatiquement par Media3
        }
        return super.onStartCommand(intent, flags, startId)
    }
}