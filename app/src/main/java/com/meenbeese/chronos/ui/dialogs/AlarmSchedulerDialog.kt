package com.meenbeese.chronos.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import com.meenbeese.chronos.data.Preferences

import java.util.Calendar

@Composable
fun AlarmSchedulerDialog(
    onDismiss: () -> Unit,
    onTimeSet: (hour: Int, minute: Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    val hourNow = calendar.get(Calendar.HOUR_OF_DAY)
    val minuteNow = calendar.get(Calendar.MINUTE)

    TimeChooserDialog(
        initialHour = hourNow,
        initialMinute = minuteNow,
        is24HourClock = Preferences.MILITARY_TIME.get(LocalContext.current),
        onDismissRequest = onDismiss,
        onTimeSet = onTimeSet
    )
}
