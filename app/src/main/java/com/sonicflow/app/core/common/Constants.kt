package com.sonicflow.app.core.common

object Constants {
    // Permissions
    const val PERMISSION_READ_MEDIA_AUDIO = android.Manifest.permission.READ_MEDIA_AUDIO

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "sonicflow_playback"
    const val NOTIFICATION_CHANNEL_NAME = "Music Playback"
    const val NOTIFICATION_ID = 1001

    // Database
    const val DATABASE_NAME = "sonicflow_database"
    const val DATABASE_VERSION = 1

    // DataStore
    const val DATASTORE_NAME = "sonicflow_preferences"

    // Player
    const val PLAYBACK_UPDATE_INTERVAL = 100L // ms
    const val WAVEFORM_SAMPLES = 256

    // Paths
    const val LYRICS_FOLDER = "SonicFlow/Lyrics"
    const val CACHE_FOLDER = "SonicFlow/Cache"
}