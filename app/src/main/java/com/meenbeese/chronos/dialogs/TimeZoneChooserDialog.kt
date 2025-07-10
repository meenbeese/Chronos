package com.meenbeese.chronos.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

import com.meenbeese.chronos.adapters.TimeZonesList

import java.util.TimeZone

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
