package com.meenbeese.chronos.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import com.meenbeese.chronos.adapters.TimeZonesList

import java.util.TimeZone

@Composable
fun TimeZoneChooserDialog(
    initialSelected: Set<String>,
    onDismiss: () -> Unit,
    onSelectionDone: (Set<String>) -> Unit
) {
    val selected = remember { mutableStateOf(initialSelected.toMutableSet()) }

    val timeZones = remember {
        TimeZone.getAvailableIDs()
            .distinctBy { TimeZone.getTimeZone(it).rawOffset }
            .sortedBy { TimeZone.getTimeZone(it).rawOffset }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Time Zones") },
        text = {
            TimeZonesList(
                timeZones = timeZones,
                selected = selected.value,
                onSelectionChanged = { id, isChecked ->
                    if (isChecked) selected.value.add(id) else selected.value.remove(id)
                }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onSelectionDone(selected.value)
                onDismiss()
            }) {
                Text("Ok")
            }
        }
    )
}
