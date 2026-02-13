package com.sonicflow.app.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class SonicFlowDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playHistoryDao(): PlayHistoryDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE play_history ADD COLUMN playDuration INTEGER NOT NULL DEFAULT 0")
    }
}