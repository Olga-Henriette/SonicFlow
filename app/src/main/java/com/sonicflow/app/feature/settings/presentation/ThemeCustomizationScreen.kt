package com.sonicflow.app.feature.settings.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.feature.settings.components.SettingsSliderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isGradient by remember { mutableStateOf(state.preferences.customTheme.isGradient) }
    var primaryColor by remember { mutableStateOf(state.preferences.customTheme.primaryColor ?: "#6750A4") }
    var secondaryColor by remember { mutableStateOf(state.preferences.customTheme.secondaryColor ?: "#958DA5") }
    var blurAmount by remember { mutableStateOf(state.preferences.customTheme.blurAmount) }
    var alpha by remember { mutableStateOf(state.preferences.customTheme.alpha) }

    var showColorPicker by remember { mutableStateOf(false) }
    var pickingPrimary by remember { mutableStateOf(true) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            context.showToast("Image selected")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Theme") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Save button
                    TextButton(
                        onClick = {
                            viewModel.handleIntent(
                                SettingsIntent.UpdateCustomTheme(
                                    enabled = true,
                                    imageUri = selectedImageUri,
                                    isGradient = isGradient,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    blurAmount = blurAmount,
                                    alpha = alpha
                                )
                            )
                            context.showToast("Theme saved")
                            onNavigateBack()
                        }
                    ) {
                        Text("Save")
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
            // Preview
            ThemePreview(
                imageUri = selectedImageUri,
                isGradient = isGradient,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                blurAmount = blurAmount,
                alpha = alpha,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            )

            Divider()

            // Source selection
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "THEME SOURCE",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Image from gallery
                    SourceCard(
                        title = "Gallery",
                        icon = Icons.Default.Image,
                        selected = selectedImageUri != null && !isGradient,
                        onClick = {
                            isGradient = false
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Gradient
                    SourceCard(
                        title = "Gradient",
                        icon = Icons.Default.Gradient,
                        selected = isGradient,
                        onClick = { isGradient = true },
                        modifier = Modifier.weight(1f)
                    )

                    // Solid color
                    SourceCard(
                        title = "Solid",
                        icon = Icons.Default.Circle,
                        selected = !isGradient && selectedImageUri == null,
                        onClick = {
                            isGradient = false
                            selectedImageUri = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Divider()

            // Color pickers (for gradient or solid)
            if (isGradient || selectedImageUri == null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COLORS",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary color
                    ColorPickerRow(
                        title = if (isGradient) "Start Color" else "Color",
                        color = primaryColor,
                        onClick = {
                            pickingPrimary = true
                            showColorPicker = true
                        }
                    )

                    if (isGradient) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary color
                        ColorPickerRow(
                            title = "End Color",
                            color = secondaryColor,
                            onClick = {
                                pickingPrimary = false
                                showColorPicker = true
                            }
                        )
                    }
                }

                Divider()
            }

            // Effects
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EFFECTS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Blur (only for images)
                if (selectedImageUri != null) {
                    SettingsSliderItem(
                        title = "Blur",
                        subtitle = "Blur effect intensity",
                        icon = Icons.Default.BlurOn,
                        value = blurAmount,
                        valueRange = 0f..25f,
                        steps = 24,
                        onValueChange = { blurAmount = it },
                        valueLabel = { "${it.toInt()} dp" }
                    )
                }

                // Alpha
                SettingsSliderItem(
                    title = "Opacity",
                    subtitle = "Background transparency",
                    icon = Icons.Default.Opacity,
                    value = alpha,
                    valueRange = 0.3f..1f,
                    steps = 6,
                    onValueChange = { alpha = it },
                    valueLabel = { "${(it * 100).toInt()}%" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Color picker dialog
    if (showColorPicker) {
        ColorPickerDialogHex(
            initialColor = if (pickingPrimary) primaryColor else secondaryColor,
            onColorSelected = { hex ->
                if (pickingPrimary) {
                    primaryColor = hex
                } else {
                    secondaryColor = hex
                }
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
fun ThemePreview(
    imageUri: Uri?,
    isGradient: Boolean,
    primaryColor: String,
    secondaryColor: String,
    blurAmount: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                imageUri != null -> {
                    // Image background
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(blurAmount.dp)
                            .background(Color.Black.copy(alpha = 1f - alpha))
                    )
                }
                isGradient -> {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(primaryColor.toColorInt()),
                                        Color(secondaryColor.toColorInt())
                                    )
                                )
                            )
                            .background(Color.Black.copy(alpha = 1f - alpha))
                    )
                }
                else -> {
                    // Solid color
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(primaryColor.toColorInt()))
                            .background(Color.Black.copy(alpha = 1f - alpha))
                    )
                }
            }

            // Preview content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This is how your theme will look",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SourceCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ColorPickerRow(
    title: String,
    color: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = color,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(color.toColorInt()))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun ColorPickerDialogHex(
    initialColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(initialColor.removePrefix("#")) }
    var isValid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            try {
                                Color(("#$hexInput").toColorInt())
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                )

                // Hex input
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input.filter { it.isLetterOrDigit() }.take(6)
                        isValid = try {
                            Color(("#$hexInput").toColorInt())
                            true
                        } catch (e: Exception) {
                            false
                        }
                    },
                    label = { Text("Hex Color") },
                    prefix = { Text("#") },
                    isError = !isValid,
                    supportingText = if (!isValid) {
                        { Text("Invalid hex color") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick color presets
                Text(
                    text = "Quick Colors",
                    style = MaterialTheme.typography.labelSmall
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "6750A4", "B3261E", "2196F3", "4CAF50",
                        "FFC107", "FF5722", "9C27B0", "00BCD4"
                    ).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(("#$preset").toColorInt()))
                                .clickable {
                                    hexInput = preset
                                    isValid = true
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        onColorSelected("#$hexInput")
                        onDismiss()
                    }
                },
                enabled = isValid
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}