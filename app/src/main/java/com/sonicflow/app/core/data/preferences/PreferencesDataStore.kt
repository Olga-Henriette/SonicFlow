package com.sonicflow.app.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sonicflow_preferences")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Obtenir toutes les préférences
     */
    val preferencesFlow: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    /**
     * Obtenir une valeur spécifique
     */
    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return preferencesFlow.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    /**
     * Sauvegarder une valeur
     */
    suspend fun <T> savePreference(key: Preferences.Key<T>, value: T) {
        try {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
            Timber.d("Preference saved: $key = $value")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save preference")
        }
    }

    /**
     * Supprimer une valeur
     */
    suspend fun <T> removePreference(key: Preferences.Key<T>) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(key)
            }
            Timber.d("Preference removed: $key")
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove preference")
        }
    }

    /**
     * Effacer toutes les préférences
     */
    suspend fun clearAll() {
        try {
            dataStore.edit { it.clear() }
            Timber.d("All preferences cleared")
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear preferences")
        }
    }
}