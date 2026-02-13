package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

class ClearPlayHistoryUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend fun clearAll() {
        repository.clearAllHistory()
    }

    suspend fun clearRecentlyPlayed() {
        repository.clearRecentlyPlayed()
    }

    suspend fun clearMostPlayed() {
        repository.clearMostPlayed()
    }
}