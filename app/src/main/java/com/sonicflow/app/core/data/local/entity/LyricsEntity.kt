package com.sonicflow.app.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey
    val songId: Long,
    val content: String,
    val source: LyricsSource,
    val language: String = "en",
    val isSynced: Boolean = false, // LRC format
    val lastModified: Long = System.currentTimeMillis(),
    val isUserEdited: Boolean = false
)

enum class LyricsSource {
    USER_INPUT,      
    LOCAL_FILE,      
    ONLINE_SEARCH,   
    AI_GENERATED     
}