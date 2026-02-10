package com.sonicflow.app.feature.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.AlbumArtImage
import com.sonicflow.app.core.common.AlbumPalette

@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    albumPalette: AlbumPalette? = null, // 🎨 Palette pour la barre de progression
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ne rien afficher si aucune chanson
    if (currentSong == null) return

    // 🎨 Animation de la couleur de la barre de progression
    val progressBarColor by animateColorAsState(
        targetValue = albumPalette?.primary ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 600),
        label = "progress bar color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        // Barre de progression
        val progress = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        // Animation fluide de la progression
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 100,
                easing = LinearEasing
            ),
            label = "progress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = progressBarColor, // 🎨 Couleur dynamique
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Contenu du mini-player
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onMiniPlayerClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            AlbumArtImage(
                albumId = currentSong.albumId,
                contentDescription = currentSong.album,
                size = 48.dp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Infos chanson
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Contrôles
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}