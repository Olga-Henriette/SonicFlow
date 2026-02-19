package com.sonicflow.app.feature.widgets.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.sonicflow.app.feature.widgets.components.MiniPlayerWidget

class MiniPlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MiniPlayerWidget()
}