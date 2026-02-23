package com.sonicflow.app.core.ui.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TopBar professionnel pour écrans de détails
 * Layout : [Back] [Title] [Search] [PlayPause] [More]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    isPlaying: Boolean = false,
    canPlay: Boolean = true,
    onPlayPauseClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (onSearchClick != null) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }

                if (onPlayPauseClick != null && canPlay) {
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (isPlaying) "Pause" else "Play all"
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select all") },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null) },
                            onClick = {
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add all to playlist") },
                            leadingIcon = { Icon(Icons.Default.PlaylistAdd, null) },
                            onClick = {
                                showMoreMenu = false
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    )
}

/**
 * Header d'informations sous le TopBar
 */
@Composable
fun DetailInfoHeader(
    title: String,
    subtitle: String? = null,
    info: String? = null,
    albumId: Long? = null,
    icon: ImageVector? = null,
    artworkSize: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artwork ou Icône
        if (albumId != null) {
            AlbumArtImage(
                albumId = albumId,
                contentDescription = title,
                size = artworkSize
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(artworkSize),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titre avec marquee
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Sous-titre
        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Info
        info?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    HorizontalDivider()
}