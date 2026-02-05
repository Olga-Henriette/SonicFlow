package com.sonicflow.app.core.common

import android.content.Context
import android.widget.Toast
import java.util.concurrent.TimeUnit

// Extension pour formater durée
fun Long.formatDuration(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format("%d:%02d", minutes, seconds)
}

// Pour formater taille fichier (bytes -> "3.5 MB")
fun Long.formatFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
        gb >= 1 -> String.format("%.2f GB", gb)
        mb >= 1 -> String.format("%.2f MB", mb)
        kb >= 1 -> String.format("%.2f KB", kb)
        else -> "$this B"
    }
}

// Pour afficher un Toast rapidement
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Pour vérifier si une String est un fichier audio
fun String.isAudioFile(): Boolean {
    val audioExtensions = listOf("mp3", "m4a", "flac", "wav", "ogg", "opus", "aac")
    return audioExtensions.any { this.endsWith(".$it", ignoreCase = true) }
}