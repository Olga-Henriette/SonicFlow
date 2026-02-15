package com.sonicflow.app.core.data.local.dao

import androidx.room.*
import com.sonicflow.app.core.data.local.entity.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics WHERE songId = :songId")
    fun getLyrics(songId: Long): Flow<LyricsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyrics(lyrics: LyricsEntity)

    @Update
    suspend fun updateLyrics(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE songId = :songId")
    suspend fun deleteLyrics(songId: Long)

    @Query("SELECT COUNT(*) FROM lyrics")
    suspend fun getLyricsCount(): Int

    @Query("SELECT * FROM lyrics WHERE isUserEdited = 1")
    fun getUserEditedLyrics(): Flow<List<LyricsEntity>>
}