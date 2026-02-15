package com.sonicflow.app.feature.player.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonicflow.app.core.common.showToast
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.ConfirmationDialog
import com.sonicflow.app.feature.player.presentation.LyricsViewModel

/**
 * Vue complète des paroles avec édition
 */
@Composable
fun LyricsView(
    song: Song?,
    modifier: Modifier = Modifier,
    viewModel: LyricsViewModel = hiltViewModel()
) {
    val lyrics by viewModel.lyrics.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val editedContent by viewModel.editedContent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    // File picker pour import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
    }

    LaunchedEffect(song?.id) {
        song?.id?.let { viewModel.loadLyrics(it) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            isEditing -> {
                // Mode édition
                LyricsEditor(
                    content = editedContent,
                    onContentChange = { viewModel.updateEditedContent(it) },
                    onSave = {
                        viewModel.saveLyrics()
                        context.showToast("Lyrics saved")
                    },
                    onCancel = { viewModel.cancelEditing() }
                )
            }

            lyrics != null -> {
                // Affichage des paroles
                LyricsDisplay(
                    content = lyrics!!.content,
                    source = lyrics!!.source.name,
                    onEdit = { viewModel.startEditing() },
                    onDelete = { showDeleteDialog = true },
                    onMore = { showOptionsMenu = true }
                )
            }

            else -> {
                // Aucune parole
                EmptyLyricsState(
                    songTitle = song?.title ?: "",
                    songArtist = song?.artist ?: "",
                    onAddManually = { viewModel.startEditing() },
                    onSearchOnline = {
                        song?.let {
                            viewModel.searchOnline(it.title, it.artist)
                            context.showToast("Searching online...")
                        }
                    },
                    onImportFile = {
                        filePickerLauncher.launch("text/*")
                    }
                )
            }
        }

        // Error snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }

    // Dialog de confirmation suppression
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Lyrics",
            message = "Are you sure you want to delete the lyrics for this song?",
            icon = Icons.Outlined.Delete,
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteLyrics()
                context.showToast("Lyrics deleted")
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    // Menu d'options
    if (showOptionsMenu) {
        LyricsOptionsMenu(
            onSearchOnline = {
                song?.let {
                    viewModel.searchOnline(it.title, it.artist)
                }
                showOptionsMenu = false
            },
            onImportFile = {
                filePickerLauncher.launch("text/*")
                showOptionsMenu = false
            },
            onDismiss = { showOptionsMenu = false }
        )
    }
}

@Composable
fun LyricsDisplay(
    content: String,
    source: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header avec actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Source: $source",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onMore) {
                    Icon(Icons.Default.MoreVert, "More")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenu des paroles
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
fun LyricsEditor(
    content: String,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel")
            }

            Button(onClick = onSave) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Champ d'édition
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Enter lyrics here...") },
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun EmptyLyricsState(
    songTitle: String,
    songArtist: String,
    onAddManually: () -> Unit,
    onSearchOnline: () -> Unit,
    onImportFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No lyrics available",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$songTitle\n$songArtist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Options
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddManually,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Outlined.Edit, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Manually")
            }

            OutlinedButton(
                onClick = onSearchOnline,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Outlined.CloudDownload, null)
                Spacer(Modifier.width(8.dp))
                Text("Search Online")
            }

            OutlinedButton(
                onClick = onImportFile,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Outlined.FileOpen, null)
                Spacer(Modifier.width(8.dp))
                Text("Import from File")
            }
        }
    }
}

@Composable
fun LyricsOptionsMenu(
    onSearchOnline: () -> Unit,
    onImportFile: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lyrics Options") },
        text = {
            Column {
                TextButton(
                    onClick = onSearchOnline,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.CloudDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search Online", modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = onImportFile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.FileOpen, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import from File", modifier = Modifier.weight(1f))
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