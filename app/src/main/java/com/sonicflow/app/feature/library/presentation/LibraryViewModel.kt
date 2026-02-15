package com.sonicflow.app.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.common.Resource
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.domain.usecase.GetAllSongsUseCase
import com.sonicflow.app.feature.library.domain.SortOption
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
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _sortOption = MutableStateFlow(SortOption.DEFAULT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    val albumCount: StateFlow<Int> = MutableStateFlow(0)
    val artistCount: StateFlow<Int> = MutableStateFlow(0)

    init {
        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getAllSongsUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is Resource.Success -> {
                        val sortedSongs = sortSongs(
                            resource.data ?: emptyList(),
                            _sortOption.value
                        )
                        _songs.value = sortedSongs
                        _isLoading.value = false
                        updateCounts(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        _songs.value = sortSongs(_songs.value, option)
    }
    private fun sortSongs(songs: List<Song>, option: SortOption): List<Song> {
        return when (option) {
            SortOption.NAME_ASC -> songs.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> songs.sortedByDescending { it.title.lowercase() }
            SortOption.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
            SortOption.ARTIST_DESC -> songs.sortedByDescending { it.artist.lowercase() }
            SortOption.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
            SortOption.DATE_ADDED_ASC -> songs.sortedBy { it.dateAdded }
            SortOption.DURATION_DESC -> songs.sortedByDescending { it.duration }
            SortOption.DURATION_ASC -> songs.sortedBy { it.duration }
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
                " - ",
                " with "
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