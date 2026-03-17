package com.meenbeese.chronos.utils

import android.content.Intent
import android.provider.AlarmClock

data class SharedTime(val hour: Int, val minute: Int)

object ShareIntentUtils {
    private val timeRegex = Regex("""\b([01]?\d|2[0-3])[:.]([0-5]\d)\s*([aApP][mM])?\b""")

    fun extractSharedTime(intent: Intent?): SharedTime? {
        if (intent == null) return null

        val alarmHour = intent.getIntExtra(AlarmClock.EXTRA_HOUR, -1)
        val alarmMinute = intent.getIntExtra(AlarmClock.EXTRA_MINUTES, -1)
        if (alarmHour in 0..23 && alarmMinute in 0..59) {
            return SharedTime(alarmHour, alarmMinute)
        }

        val candidates = buildList {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { add(it) }
            intent.getStringExtra(Intent.EXTRA_SUBJECT)?.let { add(it) }
            intent.dataString?.let { add(it) }
        }

        for (text in candidates) {
            val match = timeRegex.find(text) ?: continue
            val hour = match.groupValues[1].toIntOrNull() ?: continue
            val minute = match.groupValues[2].toIntOrNull() ?: continue
            val amPm = match.groupValues[3].lowercase()

            val normalizedHour = when (amPm) {
                "am" if hour == 12 -> 0
                "am" -> hour
                "pm" if hour < 12 -> hour + 12
                "pm" -> hour
                else -> hour
            }

            if (normalizedHour in 0..23 && minute in 0..59) {
                return SharedTime(normalizedHour, minute)
            }
        }

        return null
    }
}
