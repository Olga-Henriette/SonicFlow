package com.sonicflow.app.core.domain.usecase

import com.sonicflow.app.core.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use Case : Ajouter/Retirer une chanson des favoris
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(songId: Long) {
        repository.toggleFavorite(songId)
    }
}