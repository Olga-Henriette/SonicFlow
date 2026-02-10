package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentlyPlayedUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<Song>> {
        return repository.getRecentlyPlayed(limit)
    }
}