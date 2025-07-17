package com.meenbeese.chronos.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.ui.views.TimeNumpadItem

import java.util.concurrent.TimeUnit

@Preview
@Composable
fun DurationChooserDialog(
    onDismiss: () -> Unit = {},
    onTimeChosen: (Int, Int, Int) -> Unit = { _, _, _ -> },
    defaultHours: Int = 0,
    defaultMinutes: Int = 0,
    defaultSeconds: Int = 0
) {
    val initialInput = remember {
        val totalHours = defaultHours + TimeUnit.MINUTES.toHours(defaultMinutes.toLong()).toInt()
        val totalMinutes = (defaultMinutes % 60 + TimeUnit.SECONDS.toMinutes(defaultSeconds.toLong())).toInt()
        val totalSeconds = defaultSeconds % 60
        "%02d%02d%02d".format(totalHours, totalMinutes, totalSeconds)
    }

    var input by remember { mutableStateOf(initialInput) }

    val hours = input.substring(0, 2).toInt()
    val minutes = input.substring(2, 4).toInt()
    val seconds = input.substring(4, 6).toInt()

    val displayTime = if (hours > 0) {
        "%dh %02dm %02ds".format(hours, minutes, seconds)
    } else {
        "%dm %02ds".format(minutes, seconds)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        confirmButton = {
            TextButton(
                onClick = {
                    if (input.toInt() > 0) {
                        onTimeChosen(hours, minutes, seconds)
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.title_start_timer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0x30000000))
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = displayTime,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    if (hours != 0 || minutes != 0 || seconds != 0) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                            contentDescription = "Backspace",
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    input = "0" + input.dropLast(1)
                                },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TimeNumpadItem(
                    onDigitPressed = { digit ->
                        input = input.drop(digit.length) + digit
                    }
                )
            }
        },
        modifier = Modifier
            .widthIn(min = 300.dp, max = 500.dp)
    )
}
