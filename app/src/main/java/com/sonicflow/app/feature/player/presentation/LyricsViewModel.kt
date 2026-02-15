package com.sonicflow.app.feature.player.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.data.local.entity.LyricsEntity
import com.sonicflow.app.core.data.local.entity.LyricsSource
import com.sonicflow.app.core.domain.usecase.lyrics.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val saveLyricsUseCase: SaveLyricsUseCase,
    private val deleteLyricsUseCase: DeleteLyricsUseCase,
    private val searchLyricsOnlineUseCase: SearchLyricsOnlineUseCase
) : ViewModel() {

    private val _currentSongId = MutableStateFlow<Long?>(null)

    private val _lyrics = MutableStateFlow<LyricsEntity?>(null)
    val lyrics: StateFlow<LyricsEntity?> = _lyrics.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editedContent = MutableStateFlow("")
    val editedContent: StateFlow<String> = _editedContent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Charger les paroles pour une chanson
     */
    fun loadLyrics(songId: Long) {
        _currentSongId.value = songId

        viewModelScope.launch {
            getLyricsUseCase(songId).collect { lyricsEntity ->
                _lyrics.value = lyricsEntity
                _editedContent.value = lyricsEntity?.content ?: ""
            }
        }
    }

    /**
     * Activer le mode édition
     */
    fun startEditing() {
        _isEditing.value = true
        _editedContent.value = _lyrics.value?.content ?: ""
    }

    /**
     * Annuler l'édition
     */
    fun cancelEditing() {
        _isEditing.value = false
        _editedContent.value = _lyrics.value?.content ?: ""
        _error.value = null
    }

    /**
     * Mettre à jour le contenu en cours d'édition
     */
    fun updateEditedContent(content: String) {
        _editedContent.value = content
    }

    /**
     * Sauvegarder les paroles
     */
    fun saveLyrics() {
        val songId = _currentSongId.value ?: return

        viewModelScope.launch {
            try {
                val lyricsEntity = LyricsEntity(
                    songId = songId,
                    content = _editedContent.value,
                    source = LyricsSource.USER_INPUT,
                    isUserEdited = true,
                    lastModified = System.currentTimeMillis()
                )

                saveLyricsUseCase(lyricsEntity)
                _isEditing.value = false
                _error.value = null
                Timber.d("Lyrics saved for song $songId")
            } catch (e: Exception) {
                _error.value = "Failed to save lyrics: ${e.message}"
                Timber.e(e, "Failed to save lyrics")
            }
        }
    }

    /**
     * Supprimer les paroles
     */
    fun deleteLyrics() {
        val songId = _currentSongId.value ?: return

        viewModelScope.launch {
            try {
                deleteLyricsUseCase(songId)
                _editedContent.value = ""
                _error.value = null
                Timber.d("Lyrics deleted for song $songId")
            } catch (e: Exception) {
                _error.value = "Failed to delete lyrics: ${e.message}"
                Timber.e(e, "Failed to delete lyrics")
            }
        }
    }

    /**
     * Rechercher les paroles en ligne
     */
    fun searchOnline(title: String, artist: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            searchLyricsOnlineUseCase(title, artist).collect { resource ->
                when (resource) {
                    is com.sonicflow.app.core.common.Resource.Loading -> {
                        _isLoading.value = true
                    }
                    is com.sonicflow.app.core.common.Resource.Success -> {
                        _editedContent.value = resource.data ?: ""
                        _isLoading.value = false
                    }
                    is com.sonicflow.app.core.common.Resource.Error -> {
                        _error.value = resource.message
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    /**
     * Importer depuis un fichier
     */
    fun importFromFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                } ?: throw Exception("Failed to read file")

                _editedContent.value = content
                _isLoading.value = false
                _error.value = null

                Timber.d("Lyrics imported from file")
            } catch (e: Exception) {
                _error.value = "Failed to import lyrics: ${e.message}"
                _isLoading.value = false
                Timber.e(e, "Failed to import lyrics")
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}