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
            "PREVIOUS" -> {
                val exoPlayer = playerController.getExoPlayer()
                if (exoPlayer.hasPreviousMediaItem() ||
                    exoPlayer.currentPosition > 3000) {
                    playerController.previous()
                } else {
                    Timber.d("Previous not available - ignoring")
                }
            }

            "PLAY_PAUSE" -> {
                playerController.togglePlayPause()
            }

            "NEXT" -> {
                val exoPlayer = playerController.getExoPlayer()
                if (exoPlayer.hasNextMediaItem()) {
                    playerController.next()
                } else {
                    Timber.d("Next not available - ignoring")
                }
            }

            MusicNotificationManager.ACTION_FAVORITE -> {
                Timber.d("Favorite clicked")
                // TODO
            }

            MusicNotificationManager.ACTION_CLOSE -> {
                Timber.d("Close clicked")
                playerController.pause()
                val exoPlayer = playerController.getExoPlayer()
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            }
        }
    }
}