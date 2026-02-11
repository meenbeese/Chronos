package com.meenbeese.chronos.ui.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

import kotlin.time.Clock

class DigitalClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DigitalClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    DigitalClockWidget().updateAll(context)
                    scheduleNextUpdate(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE = "com.meenbeese.chronos.CLOCK_WIDGET_UPDATE"
    }
}

class DigitalClockWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                ClockContent()
            }
        }
    }
}

@Composable
private fun ClockContent() {
    val context = LocalContext.current
    val timeZone = TimeZone.currentSystemDefault()

    val time = FormatUtils.format(
        context = context,
        instant = Clock.System.now(),
        tz = timeZone
    )

    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            style = TextStyle(
                fontSize = 36.sp,
                fontFamily = FontFamily.SansSerif,
                color = ColorProvider(android.R.color.white)
            )
        )
    }
}

private fun scheduleNextUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, DigitalClockWidgetReceiver::class.java).apply {
        action = DigitalClockWidgetReceiver.ACTION_UPDATE
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val now = System.currentTimeMillis()
    val nextSecond = now + (1_000 - (now % 1_000))

    try {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            nextSecond,
            pendingIntent
        )
    } catch (_: SecurityException) {
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            nextSecond,
            pendingIntent
        )
    }
}

private fun cancelUpdates(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, DigitalClockWidgetReceiver::class.java).apply {
        action = DigitalClockWidgetReceiver.ACTION_UPDATE
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.cancel(pendingIntent)
}
