package com.meenbeese.chronos.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.meenbeese.chronos.data.Preferences
import java.util.Calendar

@Composable
fun AlarmSchedulerDialog(
    onDismiss: () -> Unit,
    onTimeSet: (hour: Int, minute: Int, preMinutes: Int, preText: String?) -> Unit
) {
    val calendar = Calendar.getInstance()
    val hourNow = calendar.get(Calendar.HOUR_OF_DAY)
    val minuteNow = calendar.get(Calendar.MINUTE)

    // Hold chosen time from TimeChooserDialog
    var pickedHour by remember { mutableIntStateOf(hourNow) }
    var pickedMinute by remember { mutableIntStateOf(minuteNow) }
    var showPreConfig by remember { mutableStateOf(false) }

    // Pre-notification inputs
    var preMinutesText by remember { mutableStateOf("0") }
    var preText by remember { mutableStateOf("") }

    // Step 1: Pick time
    if (!showPreConfig) {
        TimeChooserDialog(
            initialHour = hourNow,
            initialMinute = minuteNow,
            is24HourClock = Preferences.MILITARY_TIME.get(LocalContext.current),
            onDismissRequest = onDismiss,
            onTimeSet = { h, m ->
                pickedHour = h
                pickedMinute = m
                showPreConfig = true
            }
        )
    }

    // Step 2: Configure pre-notification
    if (showPreConfig) {
        AlertDialog(
            onDismissRequest = {
                // Treat dismiss as “no pre-notification”
                onTimeSet(pickedHour, pickedMinute, 0, null)
            },
            title = { Text(text = "Pre-notification") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = preMinutesText,
                        onValueChange = { new -> preMinutesText = new.filter { it.isDigit() } },
                        label = { Text("Minutes before alarm (0 to disable)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = preText,
                        onValueChange = { preText = it },
                        label = { Text("Pre-notification text (optional)") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "A notification will be shown this many minutes before the alarm.")
                }
            },
            confirmButton = {
                Row {
                    Button(
                        onClick = {
                            val preMinutes = preMinutesText.toIntOrNull() ?: 0
                            onTimeSet(pickedHour, pickedMinute, preMinutes.coerceAtLeast(0), preText.ifBlank { null })
                        }
                    ) { Text("Save") }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Skip -> no pre-notification
                        onTimeSet(pickedHour, pickedMinute, 0, null)
                    }
                ) { Text("Skip") }
            }
        )
    }
}