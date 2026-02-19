package com.sonicflow.app.feature.widgets.components

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.sonicflow.app.MainActivity
import com.sonicflow.app.feature.widgets.data.MusicWidgetRepository
import com.sonicflow.app.feature.widgets.data.MusicWidgetState
import kotlinx.coroutines.flow.first

class MiniPlayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = MusicWidgetRepository(context)
        val state = repository.widgetState.first()

        provideContent {
            MiniPlayerContent(state, context)
        }
    }
}

fun colorMin(c: Color) = ColorProvider(day = c, night = c)

@Composable
fun MiniPlayerContent(
    state: MusicWidgetState,
    context: Context
) {

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colorMin(Color(0xFF1C1B1F)))
            .cornerRadius(16.dp)
            .padding(16.dp)
            .clickable { context.startActivity(intent) },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .cornerRadius(8.dp)
                    .background(colorMin(Color(0xFF3A3A3A)))
            ) {}


            Spacer(GlanceModifier.height(8.dp))

            Text(
                text = state.songTitle,
                style = TextStyle(
                    color = colorMin(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(4.dp))

            Text(
                text = state.artistName,
                style = TextStyle(
                    color = colorMin(Color(0xFFB3B3B3)),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.height(12.dp))

            Row(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {

                WidgetButton("⏮")

                Spacer(GlanceModifier.width(16.dp))

                WidgetButton(
                    icon = if (state.isPlaying) "⏸" else "▶",
                    isPrimary = true
                )

                Spacer(GlanceModifier.width(16.dp))

                WidgetButton("⏭")
            }
        }
    }
}

@Composable
fun WidgetButton(
    icon: String,
    isPrimary: Boolean = false
) {
    Box(
        modifier = GlanceModifier
            .size(40.dp)
            .cornerRadius(20.dp)
            .background(
                colorMin(
                    if (isPrimary) Color(0xFF1DB954)
                    else Color(0xFF3A3A3A)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = TextStyle(
                color = colorMin(Color.White),
                fontSize = 16.sp
            )
        )
    }
}
