package com.example.leetcodewidget.ui.theme

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.example.leetcodewidget.glance.LeetcodeSolvedWidget

class LeetcodeWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LeetcodeSolvedWidget()
}