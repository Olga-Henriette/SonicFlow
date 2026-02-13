package com.sonicflow.app.feature.player.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sonicflow.app.core.common.AlbumPalette
import com.sonicflow.app.core.domain.model.Song
import com.sonicflow.app.core.ui.components.AlbumArtImage

@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    albumPalette: AlbumPalette? = null,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    isClickable: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    val progressBarColor by animateColorAsState(
        targetValue = albumPalette?.primary ?: MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 600),
        label = "progress bar color"
    )

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Barre de progression
        val progress = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f

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
                .height(1.dp),
            color = progressBarColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Contenu
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isClickable, onClick = onMiniPlayerClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtImage(
                    albumId = currentSong.albumId,
                    contentDescription = currentSong.album,
                    size = 40.dp,
                    modifier = Modifier.padding(start = 6.dp,end = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    // Texte défilant pour le titre
                    MarqueeText(
                        text = currentSong.title,
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

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
}

/**
 * Composant de texte défilant (marquee)
 * Défile infiniment si le texte est trop long
 */


@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        style = style,
        maxLines = 1,
        modifier = modifier.basicMarquee(
            iterations = Int.MAX_VALUE,
            animationMode = MarqueeAnimationMode.Immediately,
            spacing = MarqueeSpacing(40.dp),
            initialDelayMillis = 1000,
            velocity = 30.dp
        )
    )
}