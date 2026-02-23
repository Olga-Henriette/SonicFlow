package com.sonicflow.app.feature.settings.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonicflow.app.core.domain.model.*
import com.sonicflow.app.core.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsState(
    val preferences: UserPreferences = UserPreferences.DEFAULT,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SettingsIntent {
    data class UpdateThemeMode(val mode: ThemeMode) : SettingsIntent()
    data class UpdateAccentColor(val color: AccentColor) : SettingsIntent()
    data class UpdateCustomTheme(
        val enabled: Boolean,
        val imageUri: Uri? = null,
        val isGradient: Boolean = false,
        val primaryColor: String? = null,
        val secondaryColor: String? = null,
        val blurAmount: Float = 0f,
        val alpha: Float = 1f
    ) : SettingsIntent()
    data class UpdateLimits(val recentPlay: Int, val recentAdd: Int, val mostPlayed: Int) : SettingsIntent()
    data class UpdateFontSettings(val style: AppFontStyle, val sizeScale: Float) : SettingsIntent()
    data class UpdateLanguage(val code: String) : SettingsIntent()
    data class UpdateWidgetSettings(val opacity: Float, val showAlbumArt: Boolean, val cornerRadius: Int) : SettingsIntent()
    data object ResetToDefaults : SettingsIntent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferences
                .catch { e ->
                    Timber.e(e, "Failed to load preferences")
                    _state.update { it.copy(error = e.message) }
                }
                .collect { prefs ->
                    _state.update { it.copy(preferences = prefs, isLoading = false) }
                }
        }
    }

    fun handleIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }

                when (intent) {
                    is SettingsIntent.UpdateThemeMode -> {
                        preferencesRepository.updateThemeMode(intent.mode.name)
                    }
                    is SettingsIntent.UpdateAccentColor -> {
                        preferencesRepository.updateAccentColor(intent.color.name)
                    }
                    is SettingsIntent.UpdateCustomTheme -> {
                        preferencesRepository.updateCustomTheme(
                            enabled = intent.enabled,
                            imageUri = intent.imageUri?.toString(),
                            isGradient = intent.isGradient,
                            primaryColor = intent.primaryColor,
                            secondaryColor = intent.secondaryColor,
                            blurAmount = intent.blurAmount,
                            alpha = intent.alpha
                        )
                    }
                    is SettingsIntent.UpdateLimits -> {
                        preferencesRepository.updateLimits(
                            intent.recentPlay,
                            intent.recentAdd,
                            intent.mostPlayed
                        )
                    }
                    is SettingsIntent.UpdateFontSettings -> {
                        preferencesRepository.updateFontSettings(
                            intent.style.name,
                            intent.sizeScale
                        )
                    }
                    is SettingsIntent.UpdateLanguage -> {
                        preferencesRepository.updateLanguage(intent.code)
                    }
                    is SettingsIntent.UpdateWidgetSettings -> {
                        preferencesRepository.updateWidgetSettings(
                            intent.opacity,
                            intent.showAlbumArt,
                            intent.cornerRadius
                        )
                    }
                    is SettingsIntent.ResetToDefaults -> {
                        preferencesRepository.resetToDefaults()
                    }
                }

                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to handle intent: $intent")
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}