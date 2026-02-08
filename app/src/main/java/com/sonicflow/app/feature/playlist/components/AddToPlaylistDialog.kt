// feature/playlist/components/AddToPlaylistDialog.kt
package com.sonicflow.app.feature.playlist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.domain.model.Playlist
import com.sonicflow.app.core.domain.model.Song

/**
 * Dialog pour ajouter une chanson à une playlist
 */
@Composable
fun AddToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    // Aucune playlist
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No playlists yet",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // Liste des playlists
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(playlists) { playlist ->
                            ListItem(
                                headlineContent = { Text(playlist.name) },
                                supportingContent = { Text("${playlist.songCount} songs") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onPlaylistSelected(playlist)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNewPlaylist) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text("New Playlist")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}