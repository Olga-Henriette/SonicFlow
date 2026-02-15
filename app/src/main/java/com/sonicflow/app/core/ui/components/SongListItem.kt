package com.sonicflow.app.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import com.sonicflow.app.core.common.formatDuration
import com.sonicflow.app.core.domain.model.Song
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    isCurrentlyPlaying: Boolean = false,
    isPlaying: Boolean = false,
    onSongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press scale"
    )

    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Indicateur de lecture animé
                if (isCurrentlyPlaying) {
                    PlayingIndicator(
                        isPlaying = isPlaying,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = song.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        },
        supportingContent = {
            Text(
                text = "${song.artist} • ${song.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyPlaying) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        leadingContent = {
            AlbumArtImage(
                albumId = song.albumId,
                contentDescription = song.album,
                size = 56.dp
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Favorite
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (song.isFavorite) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // More options
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    SongContextMenu(
                        song = song,
                        expanded = showMenu,
                        onDismiss = { showMenu = false },
                        onMoreClick = onMoreClick
                    )
                }

                Text(
                    text = song.duration.formatDuration(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isPressed = true
                onSongClick()

                GlobalScope.launch {
                    delay(100)
                    isPressed = false
                }
            }

            .then(
                if (isCurrentlyPlaying) {
                    Modifier.background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                } else {
                    Modifier
                }
            )
    )
    HorizontalDivider()
}

/**
 * Indicateur de lecture animé (3 barres)
 */
@Composable
fun PlayingIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playing")

    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->

            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300 + (index * 100),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(if (isPlaying) animatedHeight else 0.3f)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}

/**
 * Menu contextuel pour une chanson
 */
@Composable
fun SongContextMenu(
    song: Song,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onMoreClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("Add to playlist") },
            leadingIcon = {
                Icon(Icons.Default.PlaylistAdd, contentDescription = null)
            },
            onClick = {
                onDismiss()
                onMoreClick()
            }
        )

        DropdownMenuItem(
            text = { Text("Rename") },
            leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null)
            },
            onClick = {
                onDismiss()
                // TODO: Implémenter rename
            }
        )

        DropdownMenuItem(
            text = { Text("Change artwork") },
            leadingIcon = {
                Icon(Icons.Default.Image, contentDescription = null)
            },
            onClick = {
                onDismiss()
                // TODO: Implémenter artwork picker
            }
        )

        DropdownMenuItem(
            text = { Text("Share") },
            leadingIcon = {
                Icon(Icons.Default.Share, contentDescription = null)
            },
            onClick = {
                onDismiss()
                // TODO: Implémenter share
            }
        )

        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            onClick = {
                onDismiss()
                // TODO: Implémenter delete avec confirmation
            }
        )
    }
}