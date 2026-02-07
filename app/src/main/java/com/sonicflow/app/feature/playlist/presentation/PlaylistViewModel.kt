package com.sonicflow.app.feature.playlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.usecase.CreatePlaylistUseCase
import com.sonicflow.app.core.domain.usecase.DeletePlaylistUseCase
import com.sonicflow.app.core.domain.usecase.GetAllPlaylistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            getAllPlaylistsUseCase().collect { playlistList ->
                _playlists.value = playlistList
                _isLoading.value = false
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                val playlistId = createPlaylistUseCase(name)
                Timber.d("Playlist created with ID: $playlistId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to create playlist")
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                deletePlaylistUseCase(playlistId)
                Timber.d("Playlist deleted: $playlistId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete playlist")
            }
        }
    }
}