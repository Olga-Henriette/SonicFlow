package com.sonicflow.app.feature.library.presentation

import androidx.lifecycle.ViewModel
import com.sonicflow.app.core.domain.usecase.GetMostPlayedUseCase
import com.sonicflow.app.core.domain.usecase.GetRecentlyPlayedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForYouViewModel @Inject constructor(
    val getMostPlayedUseCase: GetMostPlayedUseCase,
    val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase
) : ViewModel()