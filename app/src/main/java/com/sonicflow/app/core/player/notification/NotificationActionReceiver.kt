// core/player/notification/NotificationActionReceiver.kt
package com.sonicflow.app.core.player.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media3.common.util.UnstableApi
import com.sonicflow.app.core.player.controller.PlayerController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var playerController: PlayerController

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.d("Received action: ${intent?.action}")

        when (intent?.action) {
            "PREVIOUS" -> playerController.previous()
            "PLAY_PAUSE" -> playerController.togglePlayPause()
            "NEXT" -> playerController.next()

            MusicNotificationManager.ACTION_FAVORITE -> {
                Timber.d("Favorite clicked")
                // TODO: Implémenter toggle favorite
            }

            MusicNotificationManager.ACTION_CLOSE -> {
                Timber.d("Close clicked")
                playerController.pause()
            }
        }
    }
}