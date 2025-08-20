package com.meenbeese.chronos.ui.dialogs

import androidx.compose.runtime.Composable

import com.meenbeese.chronos.data.SoundData

@Composable
fun TimerSchedulerDialog(
    onDismiss: () -> Unit,
    onTimeChosen: (hours: Int, minutes: Int, seconds: Int, ringtone: SoundData?, isVibrate: Boolean) -> Unit
) {
    TimerFactoryDialog(
        onDismiss = onDismiss,
        onTimeChosen = { h, m, s, ring, vibrate ->
            onTimeChosen(h, m, s, ring, vibrate)
        },
        defaultHours = 0,
        defaultMinutes = 1,
        defaultSeconds = 0
    )
}
