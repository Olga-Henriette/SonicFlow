package com.sonicflow.app.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pochette d'album circulaire avec bordure animée
 * La bordure pulse au rythme de la musique quand isPlaying = true
 */
@Composable
fun CircularAlbumArt(
    albumId: Long,
    isPlaying: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "album border")

    // Animation de rotation lente
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animation du pulse (heartbeat)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Bordure animée
        if (isPlaying) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 8.dp.toPx()
                val radius = (this.size.minDimension - strokeWidth) / 2f

                // Gradient circulaire qui pulse
                drawCircle(
                    brush = Brush.sweepGradient(
                        0f to primaryColor.copy(alpha = 0.3f),
                        0.25f to primaryColor,
                        0.5f to primaryColor.copy(alpha = 0.5f),
                        0.75f to primaryColor,
                        1f to primaryColor.copy(alpha = 0.3f)
                    ),
                    radius = radius * pulseScale,
                    style = Stroke(
                        width = strokeWidth * pulseScale,
                        cap = StrokeCap.Round
                    ),
                    center = center
                )
            }
        } else {
            // Bordure statique quand en pause
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 4.dp.toPx()
                val radius = (this.size.minDimension - strokeWidth) / 2f

                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(width = strokeWidth),
                    center = center
                )
            }
        }

        // Image de l'album (circulaire)
        AlbumArtImage(
            albumId = albumId,
            contentDescription = "Album artwork",
            size = size - 24.dp,
            modifier = Modifier.clip(CircleShape)
        )
    }
}