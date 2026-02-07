package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case : Récupérer toutes les playlists
 */
class GetAllPlaylistsUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }
}