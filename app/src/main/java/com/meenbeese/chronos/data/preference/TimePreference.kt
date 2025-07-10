package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.dialogs.DurationChooserDialog
import com.meenbeese.chronos.utils.FormatUtils
import com.meenbeese.chronos.views.PreferenceItem

import kotlinx.coroutines.runBlocking

import java.util.concurrent.TimeUnit

/**
 * A preference item that holds / displays a time value.
 */
@Composable
fun TimePreference(
    preference: PreferenceEntry.LongPref,
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var timeMillis by remember { mutableLongStateOf(preference.get(context)) }
    val formatted = remember(timeMillis) {
        FormatUtils.formatMillis(timeMillis).dropLast(3) // remove milliseconds
    }

    PreferenceItem(
        title = stringResource(id = titleRes),
        description = formatted,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis).toInt()
        val defaultHours = totalSeconds / 3600
        val defaultMinutes = (totalSeconds % 3600) / 60
        val defaultSeconds = totalSeconds % 60

        DurationChooserDialog(
            onDismiss = { showDialog = false },
            onTimeChosen = { hours, minutes, seconds ->
                val total = TimeUnit.HOURS.toSeconds(hours.toLong()) +
                        TimeUnit.MINUTES.toSeconds(minutes.toLong()) +
                        seconds
                val newMillis = TimeUnit.SECONDS.toMillis(total)
                runBlocking {
                    preference.set(context, newMillis)
                    timeMillis = newMillis
                }
                showDialog = false
            },
            defaultHours = defaultHours,
            defaultMinutes = defaultMinutes,
            defaultSeconds = defaultSeconds
        )
    }
}
