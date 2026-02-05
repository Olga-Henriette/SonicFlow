package com.sonicflow.app.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour dépendances système Android
 * - ContentResolver (pour MediaStore)
 * - Context (pour diverses opérations)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Fournit le ContentResolver
     * Utilisé par MediaStoreDataSource pour scanner les fichiers audio
     */
    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context
    ): ContentResolver {
        return context.contentResolver
    }
}