package com.sonicflow.app.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.common.Resource
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.usecase.GetAllSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran Library
 * - Pattern MVVM simple (pas MVI ici)
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    val albumCount: StateFlow<Int> = MutableStateFlow(0)
    val artistCount: StateFlow<Int> = MutableStateFlow(0)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            getAllSongsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _songs.value = resource.data ?: emptyList()

                        updateCounts(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = resource.message
                    }
                }
            }
        }
    }
    private fun updateCounts(songs: List<Song>) {
        viewModelScope.launch {
            val uniqueAlbums = songs.map { it.albumId }.distinct().size
            (albumCount as MutableStateFlow).value = uniqueAlbums

            val uniqueArtists = songs.map { normalizeArtistName(it.artist) }.distinct().size
            (artistCount as MutableStateFlow).value = uniqueArtists
        }
    }
    private fun normalizeArtistName(artist: String): String {
        return artist
            .lowercase()
            .trim()
            .split(
                " feat ",
                " feat. ",
                " ft ",
                " ft. ",
                " featuring ",
                " & ",
                " and ",
                " x ",
                " - "
            )
            .first()
            .trim()
    }

    fun getNormalizedArtists(): List<String> {
        return _songs.value
            .map { normalizeArtistName(it.artist) }
            .distinct()
            .sorted()
    }
}