package com.sonicflow.app.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sonicflow.app.core.data.local.dao.FavoriteDao
import com.sonicflow.app.core.data.local.dao.PlayHistoryDao
import com.sonicflow.app.core.data.local.dao.PlaylistDao
import com.sonicflow.app.core.data.local.entity.FavoriteSongEntity
import com.sonicflow.app.core.data.local.entity.PlayHistoryEntity
import com.sonicflow.app.core.data.local.entity.PlaylistEntity
import com.sonicflow.app.core.data.local.entity.PlaylistSongCrossRef

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        FavoriteSongEntity::class,
        PlayHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonicFlowDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao
}