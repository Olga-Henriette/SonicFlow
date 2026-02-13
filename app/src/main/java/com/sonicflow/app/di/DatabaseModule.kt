package com.sonicflow.app.di

import android.content.Context
import androidx.room.Room
import com.sonicflow.app.core.common.Constants
import com.sonicflow.app.core.data.local.dao.FavoriteDao
import com.sonicflow.app.core.data.local.dao.PlayHistoryDao
import com.sonicflow.app.core.data.local.dao.PlaylistDao
import com.sonicflow.app.core.data.local.database.MIGRATION_1_2
import com.sonicflow.app.core.data.local.database.SonicFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour Room Database
 * - Fournit la Database et les DAOs
 * - Singleton = une seule instance dans toute l'app
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fournit la Database Room
     */
    @Provides
    @Singleton
    fun provideSonicFlowDatabase(
        @ApplicationContext context: Context
    ): SonicFlowDatabase {
        return Room.databaseBuilder(
            context,
            SonicFlowDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration() // Dev
            .build()
    }

    /**
     * Fournit PlaylistDao depuis la Database
     */
    @Provides
    @Singleton
    fun providePlaylistDao(database: SonicFlowDatabase): PlaylistDao {
        return database.playlistDao()
    }

    /**
     * Fournit FavoriteDao depuis la Database
     */
    @Provides
    @Singleton
    fun provideFavoriteDao(database: SonicFlowDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    /**
     * Fournit PlayHistoryDao depuis la Database
     */
    @Provides
    @Singleton
    fun providePlayHistoryDao(database: SonicFlowDatabase): PlayHistoryDao {
        return database.playHistoryDao()
    }
}