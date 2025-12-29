package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.ui.views.PreferenceItem
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.runBlocking

import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun RadioPreference(
    preference: PreferenceEntry.LongPref,
    @StringRes titleRes: Int,
    options: List<Duration>,
    modifier: Modifier = Modifier,
    formatOption: @Composable (Duration) -> String = {
        FormatUtils.formatMillis(
            it.toLong(DurationUnit.MILLISECONDS)
        ).take(3)
    }
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedMillis by remember { mutableLongStateOf(preference.get(context)) }

    val description = remember(selectedMillis) {
        FormatUtils.formatMillis(selectedMillis).dropLast(3)
    }

    PreferenceItem(
        title = stringResource(titleRes),
        description = description,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(titleRes)) },
            text = {
                Column {
                    options.forEach { option ->
                        val optionMillis = option.toLong(DurationUnit.MILLISECONDS)
                        val selected = optionMillis == selectedMillis

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    runBlocking {
                                        preference.set(context, optionMillis)
                                        selectedMillis = optionMillis
                                    }
                                    showDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null
                            )
                            Text(
                                text = formatOption(option),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
