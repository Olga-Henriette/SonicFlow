package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

class RemoveSongFromPlaylistUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long) {
        repository.removeSongFromPlaylist(playlistId, songId)
    }
}