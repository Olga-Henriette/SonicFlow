package com.sonicflow.app.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.sonicflow.app.core.data.local.entity.PlayHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {

    @Query("SELECT songId FROM play_history GROUP BY songId ORDER BY MAX(playedAt) DESC LIMIT :limit")
    fun getRecentlyPlayedSongIds(limit: Int): Flow<List<Long>>

    @Query("SELECT songId FROM play_history GROUP BY songId ORDER BY SUM(playCount) DESC LIMIT :limit")
    fun getMostPlayedSongIds(limit: Int): Flow<List<Long>>

    @Insert
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Query("DELETE FROM play_history WHERE playedAt < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)

    @Transaction
    suspend fun incrementPlayCount(songId: Long) {
        insertPlayHistory(
            PlayHistoryEntity(
                songId = songId,
                playedAt = System.currentTimeMillis(),
                playCount = 1
            )
        )
    }
}