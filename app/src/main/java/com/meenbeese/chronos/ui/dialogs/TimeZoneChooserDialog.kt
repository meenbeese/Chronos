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

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import kotlin.math.abs

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

    val timeZones = remember {
        TimeZone.getAvailableIDs()
            .distinctBy { TimeZone.getTimeZone(it).displayName }
            .filterNot { TimeZone.getTimeZone(it).displayName.startsWith("GMT") }
            .sortedBy { TimeZone.getTimeZone(it).rawOffset }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Time Zones") },
        text = {
            TimeZonesList(
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
