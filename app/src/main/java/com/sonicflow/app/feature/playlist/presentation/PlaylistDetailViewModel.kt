package com.sonicflow.app.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import com.sonicflow.app.core.domain.usecase.GetPlaylistSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    val getPlaylistSongsUseCase: GetPlaylistSongsUseCase
) : ViewModel()