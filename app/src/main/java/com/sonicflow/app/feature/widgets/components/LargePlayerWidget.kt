package com.sonicflow.app.feature.widgets.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.sonicflow.app.feature.widgets.data.MusicWidgetRepository
import com.sonicflow.app.feature.widgets.data.MusicWidgetState
import kotlinx.coroutines.flow.first

class LargePlayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = MusicWidgetRepository(context)
        val state = repository.widgetState.first()

        provideContent {
            LargePlayerContent(state)
        }
    }
}

fun color(c: Color) = ColorProvider(day = c, night = c)

@Composable
fun LargePlayerContent(state: MusicWidgetState) {

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(color(Color(0xFF1C1B1F)))
            .cornerRadius(16.dp)
            .padding(16.dp)
    ) {

        Column(
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {

            Text(
                text = state.songTitle,
                style = TextStyle(
                    color = color(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = state.artistName,
                style = TextStyle(
                    color = color(Color(0xFFB3B3B3)),
                    fontSize = 14.sp
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(16.dp))

            Row(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {

                WidgetButton("🔀")

                Spacer(GlanceModifier.width(12.dp))

                WidgetButton("⏮")

                Spacer(GlanceModifier.width(12.dp))

                WidgetButton(
                    icon = if (state.isPlaying) "⏸" else "▶",
                    isPrimary = true
                )

                Spacer(GlanceModifier.width(12.dp))

                WidgetButton("⏭")

                Spacer(GlanceModifier.width(12.dp))

                WidgetButton("❤")
            }
        }
    }
}
