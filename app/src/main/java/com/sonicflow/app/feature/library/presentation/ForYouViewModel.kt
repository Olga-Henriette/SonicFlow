package com.sonicflow.app.feature.library.presentation

import androidx.lifecycle.ViewModel
import com.sonicflow.app.core.domain.usecase.ClearPlayHistoryUseCase
import com.sonicflow.app.core.domain.usecase.GetMostPlayedUseCase
import com.sonicflow.app.core.domain.usecase.GetRecentlyPlayedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toSet
import javax.inject.Inject

@HiltViewModel
class ForYouViewModel @Inject constructor(
    val getMostPlayedUseCase: GetMostPlayedUseCase,
    val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    val clearPlayHistoryUseCase: ClearPlayHistoryUseCase,
    private val musicRepository: com.sonicflow.app.core.domain.repository.MusicRepository
) : ViewModel(){

    // Mix Découverte : musiques les plus écoutées, mais mélangées pour varier
    val dailyMix = getMostPlayedUseCase(20).map { songs ->
        songs.shuffled().take(10)
    }

    // Coups de cœur oubliés : Chansons très écoutées qui ne sont PAS dans le "Recently Played"
    val forgottenFavorites = combine(
        getMostPlayedUseCase(30),
        getRecentlyPlayedUseCase(20)
    ) { most, recent ->
        val recentIds = recent.map { it.id }.toSet()
        most.filter { it.id !in recentIds }.take(10)
    }
}