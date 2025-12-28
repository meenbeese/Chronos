package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.ui.dialogs.SoundChooserDialog
import com.meenbeese.chronos.ui.dialogs.TimeChooserDialog
import com.meenbeese.chronos.ui.views.AlarmItemView

import java.util.Calendar

@Composable
fun AlarmListScreen(
    alarms: List<AlarmData>,
    onAlarmUpdated: (AlarmData) -> Unit,
    onAlarmDeleted: (AlarmData) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val context = LocalContext.current
    var expandedAlarmId by rememberSaveable { mutableStateOf<Int?>(null) }
    var timePickerAlarm by remember { mutableStateOf<AlarmData?>(null) }
    var soundPickerAlarm by remember { mutableStateOf<AlarmData?>(null) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(alarms, key = { it.id }) { alarm ->
            val isExpanded = expandedAlarmId == alarm.id

            AlarmItemView(
                alarm = alarm,
                isExpanded = isExpanded,
                onAlarmUpdated = onAlarmUpdated,
                onTimeClick = { timePickerAlarm = alarm },
                onToggleEnabled = { enabled ->
                    alarm.isEnabled = enabled
                    onAlarmUpdated(alarm)
                },
                onRingtoneClick = { soundPickerAlarm = alarm },
                onVibrateToggle = {
                    alarm.isVibrate = !alarm.isVibrate
                    onAlarmUpdated(alarm)
                },
                onDeleteClick = { onAlarmDeleted(alarm) },
                onExpandClick = {
                    expandedAlarmId = if (isExpanded) null else alarm.id
                }
            )
        }
    }

    timePickerAlarm?.let { alarm ->
        val hour = alarm.time.get(Calendar.HOUR_OF_DAY)
        val minute = alarm.time.get(Calendar.MINUTE)

        TimeChooserDialog(
            initialHour = hour,
            initialMinute = minute,
            is24HourClock = Preferences.MILITARY_TIME.get(context),
            onDismissRequest = { timePickerAlarm = null },
            onTimeSet = { newHour, newMinute ->
                alarm.time.set(Calendar.HOUR_OF_DAY, newHour)
                alarm.time.set(Calendar.MINUTE, newMinute)
                alarm.time.set(Calendar.SECOND, 0)
                alarm.time.set(Calendar.MILLISECOND, 0)
                onAlarmUpdated(alarm)
                timePickerAlarm = null
            }
        )
    }

    soundPickerAlarm?.let { alarm ->
        SoundChooserDialog(
            onDismissRequest = { soundPickerAlarm = null },
            onSoundChosen = { sound ->
                alarm.sound = sound
                onAlarmUpdated(alarm)
                soundPickerAlarm = null
            }
        )
    }
}
