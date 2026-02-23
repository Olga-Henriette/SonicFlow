package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.common.Resource
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<Resource<List<Song>>> {
        return repository.getAllSongs()
    }
}