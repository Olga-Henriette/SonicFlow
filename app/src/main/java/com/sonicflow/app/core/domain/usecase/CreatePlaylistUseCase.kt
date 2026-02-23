package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(name: String): Long {
        require(name.isNotBlank()) { "Playlist name cannot be empty" }
        return repository.createPlaylist(name)
    }
}