package com.sonicflow.app.feature.player.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 50
) {

    val animatedValues = rememberAnimatedAmplitudes(
        barCount = barCount,
        durationBase = 400,
        label = "waveform"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {

        val barWidth = size.width / (barCount * 2f)
        val centerY = size.height / 2f

        animatedValues.forEachIndexed { index, state ->

            val value = state.value

            val height = if (isPlaying) {
                (value * size.height * 0.8f).coerceIn(4f, size.height * 0.8f)
            } else {
                size.height * 0.1f
            }

            val x = index * barWidth * 2f + barWidth

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        primaryColor.copy(alpha = 0.3f)
                    ),
                    startY = centerY - height / 2f,
                    endY = centerY + height / 2f
                ),
                start = Offset(x, centerY - height / 2f),
                end = Offset(x, centerY + height / 2f),
                strokeWidth = barWidth * 0.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun CircularWaveformVisualizer(
    isPlaying: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 60
) {

    val animatedValues = rememberAnimatedAmplitudes(
        barCount = barCount,
        durationBase = 300,
        label = "circular"
    )

    Canvas(modifier = modifier.size(200.dp)) {

        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.width / 3f

        animatedValues.forEachIndexed { index, state ->

            val angle = (index.toFloat() / barCount) * 2f * Math.PI.toFloat()

            val value = state.value

            val length = if (isPlaying) value * radius else radius * 0.2f

            val startX = centerX + kotlin.math.cos(angle) * radius
            val startY = centerY + kotlin.math.sin(angle) * radius
            val endX = centerX + kotlin.math.cos(angle) * (radius + length)
            val endY = centerY + kotlin.math.sin(angle) * (radius + length)

            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.3f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius + length
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun rememberAnimatedAmplitudes(
    barCount: Int,
    durationBase: Int = 400,
    label: String
): List<State<Float>> {

    val infiniteTransition = rememberInfiniteTransition(label = label)

    val amplitudes = remember {
        List(barCount) { Random.nextFloat() }
    }

    return amplitudes.mapIndexed { index, baseAmplitude ->
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = baseAmplitude,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationBase + (index % 10) * 30,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "$label-$index"
        )
    }
}
