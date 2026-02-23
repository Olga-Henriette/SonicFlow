package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

class AddSongToPlaylistUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long) {
        repository.addSongToPlaylist(playlistId, songId)
    }
}