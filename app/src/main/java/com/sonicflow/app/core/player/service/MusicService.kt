package com.sonicflow.app.core.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonicflow.app.MainActivity
import com.sonicflow.app.core.player.controller.PlayerController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Service de lecture audio en arrière-plan
 * - Survit même si l'app est fermée
 * - Gère MediaSession (contrôles lockscreen, Bluetooth, etc.)
 * - Notification obligatoire (foreground service)
 */
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var playerController: PlayerController

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("MusicService onCreate")

        // Configurer les attributs audio (optimisé pour la musique)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        playerController.getExoPlayer().setAudioAttributes(audioAttributes, true)

        // Créer le PendingIntent pour ouvrir l'app depuis la notification
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // Créer MediaSession
        mediaSession = MediaSession.Builder(this, playerController.getExoPlayer())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        Timber.d("MediaSession created")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
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
}