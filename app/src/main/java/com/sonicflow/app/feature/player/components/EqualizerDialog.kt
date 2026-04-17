package com.sonicflow.app.feature.player.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.player.controller.EqualizerController
import com.sonicflow.app.feature.player.presentation.EqualizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerDialog(
    onDismiss: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showPresets by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                EqualizerHeader(
                    isEnabled = state.isEnabled,
                    selectedPreset = state.selectedPreset,
                    onToggle = { viewModel.toggleEnabled() },
                    onPresetsClick = { showPresets = !showPresets },
                    onResetClick = { viewModel.reset() },
                    onClose = onDismiss
                )

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Presets
                    AnimatedVisibility(
                        visible = showPresets,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        PresetsGrid(
                            selectedPreset = state.selectedPreset,
                            onPresetSelected = { preset ->
                                viewModel.applyPreset(preset)
                                showPresets = false
                            }
                        )
                    }

                    // Bandes d'égalisation
                    Text(
                        text = "Equalizer Bands",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    EqualizerBands(
                        enabled = state.isEnabled,
                        numberOfBands = state.numberOfBands,
                        bandLevels = state.bandLevels,
                        bandLevelRange = state.bandLevelRange,
                        onBandLevelChange = { band, level ->
                            viewModel.setBandLevel(band, level)
                        },
                        getBandFrequency = { band ->
                            viewModel.getBandFrequency(band)
                        }
                    )

                    Divider()

                    // Bass Boost
                    Text(
                        text = "Bass Boost",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    BassBoostControl(
                        enabled = state.isEnabled,
                        strength = state.bassBoostStrength,
                        onStrengthChange = { viewModel.setBassBoost(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun EqualizerHeader(
    isEnabled: Boolean,
    selectedPreset: String,
    onToggle: () -> Unit,
    onPresetsClick: () -> Unit,
    onResetClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Première ligne: Titre + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedPreset,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deuxième ligne: Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch Enable/Disable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Enable")
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle() }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton Presets
                    FilledTonalButton(onClick = onPresetsClick) {
                        Icon(Icons.Default.LibraryMusic, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Presets")
                    }

                    // Bouton Reset
                    OutlinedButton(onClick = onResetClick) {
                        Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PresetsGrid(
    selectedPreset: String,
    onPresetSelected: (EqualizerController.EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Grid 2 colonnes
        EqualizerController.ALL_PRESETS.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = preset.name == selectedPreset,
                        onClick = { onPresetSelected(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Remplir si impair
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PresetCard(
    preset: EqualizerController.EqualizerPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
fun EqualizerBands(
    enabled: Boolean,
    numberOfBands: Int,
    bandLevels: List<Short>,
    bandLevelRange: Pair<Short, Short>,
    onBandLevelChange: (Int, Short) -> Unit,
    getBandFrequency: (Int) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (band in 0 until numberOfBands) {
            Box(modifier = Modifier.weight(1f)) {
                EqualizerBand(
                    enabled = enabled,
                    bandIndex = band,
                    level = bandLevels.getOrNull(band) ?: 0,
                    minLevel = bandLevelRange.first,
                    maxLevel = bandLevelRange.second,
                    frequency = getBandFrequency(band),
                    onLevelChange = { level -> onBandLevelChange(band, level) }
                )
            }
        }
    }
}

@Composable
fun EqualizerBand(
    enabled: Boolean,
    bandIndex: Int,
    level: Short,
    minLevel: Short,
    maxLevel: Short,
    frequency: String,
    onLevelChange: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "${level / 100}dB", style = MaterialTheme.typography.labelSmall)

        // Zone tactile personnalisée
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures { offset ->
                        // Calcul du niveau selon l'endroit où on appuie
                        val ratio = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                        val newLevel = (minLevel + (maxLevel - minLevel) * ratio).toInt().toShort()
                        onLevelChange(newLevel)
                    }
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures { change, _ ->
                        // Calcul du niveau pendant qu'on glisse
                        val ratio = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                        val newLevel = (minLevel + (maxLevel - minLevel) * ratio).toInt().toShort()
                        onLevelChange(newLevel)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Fond de la barre
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            )

            // Barre de progression (Dégradé)
            val normalizedLevel = ((level - minLevel).toFloat() / (maxLevel - minLevel).toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(normalizedLevel)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )
        }

        Text(
            text = frequency.replace(" ", "\n"),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun BassBoostControl(
    enabled: Boolean,
    strength: Short,
    onStrengthChange: (Short) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Strength",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${(strength / 10)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = strength.toFloat(),
            onValueChange = { onStrengthChange(it.toInt().toShort()) },
            valueRange = 0f..1000f,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}