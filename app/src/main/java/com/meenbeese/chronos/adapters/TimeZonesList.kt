package com.meenbeese.chronos.adapters

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable

import com.meenbeese.chronos.views.TimeZoneItem

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import kotlin.math.abs

@Composable
fun TimeZonesList(
    timeZones: List<String>,
    selected: MutableSet<String>,
    onSelectionChanged: (String, Boolean) -> Unit
) {
    LazyColumn {
        items(timeZones) { timeZoneId ->
            val timeZone = TimeZone.getTimeZone(timeZoneId)
            val offsetMillis = timeZone.rawOffset

            val offsetFormatted = String.format(
                Locale.getDefault(),
                "GMT%s%02d:%02d",
                if (offsetMillis >= 0) "+" else "-",
                TimeUnit.MILLISECONDS.toHours(abs(offsetMillis.toLong())),
                TimeUnit.MILLISECONDS.toMinutes(abs(offsetMillis.toLong())) % TimeUnit.HOURS.toMinutes(1)
            )

            val title = timeZone.getDisplayName(Locale.getDefault())
            val isChecked = selected.contains(timeZoneId)

            TimeZoneItem(
                timeText = offsetFormatted,
                titleText = title,
                isChecked = isChecked,
                onCheckedChange = { checked ->
                    if (checked) selected.add(timeZoneId) else selected.remove(timeZoneId)
                    onSelectionChanged(timeZoneId, checked)
                }
            )
        }
    }
}
