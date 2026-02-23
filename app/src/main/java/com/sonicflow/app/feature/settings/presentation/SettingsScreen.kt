package com.sonicflow.app.feature.settings.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.BuildConfig
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.domain.model.*
import com.sonicflow.app.feature.settings.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToThemeCustomization: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFontStyleDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // APPEARANCE SECTION
            SettingsSection(title = "APPEARANCE") {

                SettingsSwitchItem(
                    title = "Custom Theme",
                    subtitle = if (state.preferences.customTheme.isEnabled) "Enabled" else "Disabled",
                    icon = Icons.Default.Palette,
                    checked = state.preferences.customTheme.isEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateCustomTheme(
                                enabled = enabled,
                                imageUri = state.preferences.customTheme.imageUri?.let { Uri.parse(it) },
                                isGradient = state.preferences.customTheme.isGradient,
                                primaryColor = state.preferences.customTheme.primaryColor,
                                secondaryColor = state.preferences.customTheme.secondaryColor,
                                blurAmount = state.preferences.customTheme.blurAmount,
                                alpha = state.preferences.customTheme.alpha
                            )
                        )
                    }
                )

                if (state.preferences.customTheme.isEnabled) {
                    SettingsItem(
                        title = "Customize Theme",
                        subtitle = "Choose colors, images, and effects",
                        icon = Icons.Default.Edit,
                        onClick = onNavigateToThemeCustomization
                    )
                }

                SettingsItem(
                    title = "Theme Mode",
                    subtitle = state.preferences.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Palette,
                    onClick = { showThemeModeDialog = true }
                )

                SettingsItem(
                    title = "Accent Color",
                    subtitle = state.preferences.accentColor.displayName,
                    icon = Icons.Default.FormatPaint,
                    onClick = { showColorPicker = true }
                )

                SettingsSliderItem(
                    title = "Font Size",
                    subtitle = "Adjust text size throughout the app",
                    icon = Icons.Default.FormatSize,
                    value = state.preferences.fontSettings.sizeScale,
                    valueRange = 0.8f..1.5f,
                    steps = 6,
                    onValueChange = { scale ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateFontSettings(
                                state.preferences.fontSettings.style,
                                scale
                            )
                        )
                    },
                    valueLabel = { "${(it * 100).toInt()}%" }
                )

                SettingsItem(
                    title = "Font Style",
                    subtitle = state.preferences.fontSettings.style.displayName,
                    icon = Icons.Default.TextFields,
                    onClick = { showFontStyleDialog = true }
                )
            }

            Divider()

            // LIBRARY SECTION
            SettingsSection(title = "LIBRARY") {
                SettingsSliderItem(
                    title = "Recently Played Limit",
                    subtitle = "Number of songs to show",
                    icon = Icons.Default.History,
                    value = state.preferences.recentPlayLimit.toFloat(),
                    valueRange = 5f..50f,
                    steps = 8,
                    onValueChange = { value ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateLimits(
                                recentPlay = value.toInt(),
                                recentAdd = state.preferences.recentAddLimit,
                                mostPlayed = state.preferences.mostPlayedLimit
                            )
                        )
                    },
                    valueLabel = { it.toInt().toString() }
                )

                SettingsSliderItem(
                    title = "Recently Added Limit",
                    subtitle = "Number of songs to show",
                    icon = Icons.Default.NewReleases,
                    value = state.preferences.recentAddLimit.toFloat(),
                    valueRange = 5f..50f,
                    steps = 8,
                    onValueChange = { value ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateLimits(
                                recentPlay = state.preferences.recentPlayLimit,
                                recentAdd = value.toInt(),
                                mostPlayed = state.preferences.mostPlayedLimit
                            )
                        )
                    },
                    valueLabel = { it.toInt().toString() }
                )

                SettingsSliderItem(
                    title = "Most Played Limit",
                    subtitle = "Number of songs to show",
                    icon = Icons.Default.TrendingUp,
                    value = state.preferences.mostPlayedLimit.toFloat(),
                    valueRange = 5f..50f,
                    steps = 8,
                    onValueChange = { value ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateLimits(
                                recentPlay = state.preferences.recentPlayLimit,
                                recentAdd = state.preferences.recentAddLimit,
                                mostPlayed = value.toInt()
                            )
                        )
                    },
                    valueLabel = { it.toInt().toString() }
                )
            }

            Divider()

            // WIDGETS SECTION
            SettingsSection(title = "WIDGETS") {
                SettingsSwitchItem(
                    title = "Show Album Art",
                    subtitle = "Display album artwork on widgets",
                    icon = Icons.Default.Image,
                    checked = state.preferences.widgetShowAlbumArt,
                    onCheckedChange = { enabled ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateWidgetSettings(
                                opacity = state.preferences.widgetBackgroundOpacity,
                                showAlbumArt = enabled,
                                cornerRadius = state.preferences.widgetCornerRadius
                            )
                        )
                    }
                )

                SettingsSliderItem(
                    title = "Background Opacity",
                    subtitle = "Widget background transparency",
                    icon = Icons.Default.Opacity,
                    value = state.preferences.widgetBackgroundOpacity,
                    valueRange = 0.3f..1f,
                    steps = 6,
                    onValueChange = { opacity ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateWidgetSettings(
                                opacity = opacity,
                                showAlbumArt = state.preferences.widgetShowAlbumArt,
                                cornerRadius = state.preferences.widgetCornerRadius
                            )
                        )
                    },
                    valueLabel = { "${(it * 100).toInt()}%" }
                )

                SettingsSliderItem(
                    title = "Corner Radius",
                    subtitle = "Widget corner roundness",
                    icon = Icons.Default.RoundedCorner,
                    value = state.preferences.widgetCornerRadius.toFloat(),
                    valueRange = 0f..32f,
                    steps = 7,
                    onValueChange = { radius ->
                        viewModel.handleIntent(
                            SettingsIntent.UpdateWidgetSettings(
                                opacity = state.preferences.widgetBackgroundOpacity,
                                showAlbumArt = state.preferences.widgetShowAlbumArt,
                                cornerRadius = radius.toInt()
                            )
                        )
                    },
                    valueLabel = { "${it.toInt()} dp" }
                )
            }

            Divider()

            // GENERAL SECTION
            SettingsSection(title = "GENERAL") {
                SettingsItem(
                    title = "Language",
                    subtitle = getLanguageDisplayName(state.preferences.languageCode),
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )

                SettingsItem(
                    title = "Share App",
                    subtitle = "Share SonicFlow with friends",
                    icon = Icons.Default.Share,
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out SonicFlow - A beautiful music player for Android!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                )
            }

            Divider()

            // ABOUT SECTION
            SettingsSection(title = "ABOUT") {
                SettingsItem(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    icon = Icons.Default.Info,
                    onClick = {
                        context.showToast("SonicFlow ${BuildConfig.VERSION_NAME}")
                    }
                )

                SettingsItem(
                    title = "Reset to Defaults",
                    subtitle = "Restore all settings to default values",
                    icon = Icons.Default.RestartAlt,
                    onClick = { showResetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showThemeModeDialog) {
        ThemeModeDialog(
            selectedMode = state.preferences.themeMode,
            onModeSelected = { mode ->
                viewModel.handleIntent(SettingsIntent.UpdateThemeMode(mode))
                showThemeModeDialog = false
            },
            onDismiss = { showThemeModeDialog = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            selectedColor = state.preferences.accentColor,
            onColorSelected = { color ->
                viewModel.handleIntent(SettingsIntent.UpdateAccentColor(color))
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showFontStyleDialog) {
        FontStyleDialog(
            selectedStyle = state.preferences.fontSettings.style,
            onStyleSelected = { style ->
                viewModel.handleIntent(
                    SettingsIntent.UpdateFontSettings(
                        style,
                        state.preferences.fontSettings.sizeScale
                    )
                )
                showFontStyleDialog = false
            },
            onDismiss = { showFontStyleDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            selectedLanguage = state.preferences.languageCode,
            onLanguageSelected = { code ->
                viewModel.handleIntent(SettingsIntent.UpdateLanguage(code))
                showLanguageDialog = false
                context.showToast("Language changed. Restart app to apply.")
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Are you sure you want to reset all settings to their default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleIntent(SettingsIntent.ResetToDefaults)
                        showResetDialog = false
                        context.showToast("Settings reset to defaults")
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ThemeModeDialog(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Theme Mode") },
        text = {
            Column {
                ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                        if (mode == selectedMode) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FontStyleDialog(
    selectedStyle: AppFontStyle,
    onStyleSelected: (AppFontStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Font Style") },
        text = {
            Column {
                AppFontStyle.values().forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStyleSelected(style) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(style.displayName)
                        if (style == selectedStyle) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun LanguageDialog(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = mapOf(
        "system" to "System Default",
        "en" to "English",
        "fr" to "Français",
        "es" to "Español",
        "de" to "Deutsch",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "zh" to "中文",
        "ja" to "日本語",
        "ko" to "한국어"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Language") },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name)
                        if (code == selectedLanguage) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

fun getLanguageDisplayName(code: String): String {
    return when (code) {
        "system" -> "System Default"
        "en" -> "English"
        "fr" -> "Français"
        "es" -> "Español"
        "de" -> "Deutsch"
        "it" -> "Italiano"
        "pt" -> "Português"
        "ru" -> "Русский"
        "zh" -> "中文"
        "ja" -> "日本語"
        "ko" -> "한국어"
        else -> "System Default"
    }
}