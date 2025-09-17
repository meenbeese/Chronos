package com.meenbeese.chronos.ui.dialogs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

import com.meenbeese.chronos.ui.views.TimeZoneItem

import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt

import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

@Preview
@Composable
fun TimeZoneChooserDialog(
    initialSelected: Set<String> = emptySet(),
    onDismiss: () -> Unit = {},
    onSelectionDone: (Set<String>) -> Unit = {}
) {
    val selected = remember {
        mutableStateSetOf<String>().apply { addAll(initialSelected) }
    }

    val now = Clock.System.now()

    val timeZones = remember {
        TimeZone.availableZoneIds.sortedBy { tzId ->
            val zone = TimeZone.of(tzId)
            zone.offsetAt(now).totalSeconds
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Time Zones") },
        text = {
            TimeZonesList(
                now = now,
                timeZones = timeZones,
                selected = selected,
                onSelectionChanged = { id, isChecked ->
                    if (isChecked) selected.add(id) else selected.remove(id)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onSelectionDone(selected.toSet())
                onDismiss()
            }) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun TimeZonesList(
    now: Instant,
    timeZones: List<String>,
    selected: MutableSet<String>,
    onSelectionChanged: (String, Boolean) -> Unit
) {
    LazyColumn {
        items(timeZones) { timeZoneId ->
            val zone = TimeZone.of(timeZoneId)
            val offsetSeconds = zone.offsetAt(now).totalSeconds
            val hours = abs(offsetSeconds / 3600)
            val minutes = abs(offsetSeconds % 3600 / 60)
            val sign = if (offsetSeconds >= 0) "+" else "-"

            val offsetFormatted = "GMT$sign%02d:%02d".format(hours, minutes)
            val title = timeZoneId
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
