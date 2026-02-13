package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

class IncrementPlayCountUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(songId: Long, playDuration: Long = 0) {
        repository.incrementPlayCount(songId, playDuration)
    }
}