package com.meenbeese.chronos.ui.preferences

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

import kotlinx.coroutines.runBlocking

data class StringRadioOption(
    val value: String,
    @StringRes val labelRes: Int
)

@Composable
fun StringRadioPreference(
    preference: PreferenceEntry.StringPref,
    @StringRes titleRes: Int,
    options: List<StringRadioOption>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf(preference.get(context)) }

    val selectedLabel = options.firstOrNull { it.value == selectedValue }?.labelRes
        ?: options.firstOrNull()?.labelRes

    PreferenceItem(
        title = stringResource(titleRes),
        description = selectedLabel?.let { stringResource(it) },
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
                        val selected = option.value == selectedValue

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    runBlocking {
                                        preference.set(context, option.value)
                                        selectedValue = option.value
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
                                text = stringResource(option.labelRes),
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
