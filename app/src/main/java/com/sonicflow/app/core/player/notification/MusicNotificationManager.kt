// core/player/notification/MusicNotificationManager.kt
package com.sonicflow.app.core.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.sonicflow.app.MainActivity
import com.sonicflow.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MusicNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "sonicflow_playback"
        const val NOTIFICATION_ID = 1001

        const val ACTION_FAVORITE = "com.sonicflow.app.ACTION_FAVORITE"
        const val ACTION_CLOSE = "com.sonicflow.app.ACTION_CLOSE"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                android.app.NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for music playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
            Timber.d("Notification channel created")
        }
    }

    fun createNotificationProvider(): MediaNotification.Provider {
        return object : MediaNotification.Provider {

            override fun createNotification(
                mediaSession: MediaSession,
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                val notification = createBaseNotification(mediaSession)
                return MediaNotification(NOTIFICATION_ID, notification)
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: android.os.Bundle
            ): Boolean {
                when (action) {
                    ACTION_FAVORITE -> {
                        Timber.d("Favorite action triggered")
                        return true
                    }
                    ACTION_CLOSE -> {
                        session.player.stop()
                        session.player.clearMediaItems()
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun createBaseNotification(mediaSession: MediaSession): Notification {
        val player = mediaSession.player
        val metadata = player.currentMediaItem?.mediaMetadata

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(metadata?.title?.toString() ?: "No song playing")
            .setContentText(metadata?.artist?.toString() ?: "Unknown artist")
            .setSubText(metadata?.albumTitle?.toString())
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setAutoCancel(false)
            .setOngoing(player.isPlaying)
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        // Previous
        builder.addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_previous,
                "Previous",
                createPendingIntent("PREVIOUS")
            ).build()
        )

        // Play/Pause
        builder.addAction(
            NotificationCompat.Action.Builder(
                if (player.isPlaying) {
                    android.R.drawable.ic_media_pause
                } else {
                    android.R.drawable.ic_media_play
                },
                if (player.isPlaying) "Pause" else "Play",
                createPendingIntent("PLAY_PAUSE")
            ).build()
        )

        // Next
        builder.addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_next,
                "Next",
                createPendingIntent("NEXT")
            ).build()
        )

        // Favorite ⭐
        builder.addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.star_big_off,
                "Favorite",
                createPendingIntent(ACTION_FAVORITE)
            ).build()
        )

        // Close ❌
        builder.addAction(
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Close",
                createPendingIntent(ACTION_CLOSE)
            ).build()
        )

        return builder.build()
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}