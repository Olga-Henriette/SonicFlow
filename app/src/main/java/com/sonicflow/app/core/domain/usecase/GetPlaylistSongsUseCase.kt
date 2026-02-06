package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case : Récupérer les chansons d'une playlist
 */
class GetPlaylistSongsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(playlistId: Long): Flow<List<Song>> {
        return repository.getPlaylistSongs(playlistId)
    }
}