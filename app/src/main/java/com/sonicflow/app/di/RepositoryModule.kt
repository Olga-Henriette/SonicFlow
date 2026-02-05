package com.sonicflow.app.di

import com.sonicflow.app.core.data.repository.MusicRepositoryImpl
import com.sonicflow.app.core.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour les Repositories
 * - Lie l'interface MusicRepository à son implémentation
 * - Utilise @Binds (plus performant que @Provides pour les interfaces)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Lie MusicRepository (interface) → MusicRepositoryImpl (implémentation)
     * Quand quelqu'un demande MusicRepository, Hilt fournit MusicRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository
}