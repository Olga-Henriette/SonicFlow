package com.sonicflow.app.feature.widgets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * État du widget de musique
 * Persisté avec DataStore
 */
data class MusicWidgetState(
    val songTitle: String = "No song playing",
    val artistName: String = "Unknown artist",
    val albumId: Long = 0L,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "music_widget_prefs"
)

/**
 * Repository pour l'état du widget
 */
class MusicWidgetRepository(private val context: Context) {

    companion object {
        private val SONG_TITLE = stringPreferencesKey("song_title")
        private val ARTIST_NAME = stringPreferencesKey("artist_name")
        private val ALBUM_ID = longPreferencesKey("album_id")
        private val IS_PLAYING = booleanPreferencesKey("is_playing")
        private val CURRENT_POSITION = longPreferencesKey("current_position")
        private val DURATION = longPreferencesKey("duration")
    }

    val widgetState: Flow<MusicWidgetState> = context.widgetDataStore.data.map { prefs ->
        MusicWidgetState(
            songTitle = prefs[SONG_TITLE] ?: "No song playing",
            artistName = prefs[ARTIST_NAME] ?: "Unknown artist",
            albumId = prefs[ALBUM_ID] ?: 0L,
            isPlaying = prefs[IS_PLAYING] ?: false,
            currentPosition = prefs[CURRENT_POSITION] ?: 0L,
            duration = prefs[DURATION] ?: 0L
        )
    }

    suspend fun updateState(state: MusicWidgetState) {
        context.widgetDataStore.edit { prefs ->
            prefs[SONG_TITLE] = state.songTitle
            prefs[ARTIST_NAME] = state.artistName
            prefs[ALBUM_ID] = state.albumId
            prefs[IS_PLAYING] = state.isPlaying
            prefs[CURRENT_POSITION] = state.currentPosition
            prefs[DURATION] = state.duration
        }
    }
}