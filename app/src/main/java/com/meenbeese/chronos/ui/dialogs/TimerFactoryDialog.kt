package com.meenbeese.chronos.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ui.views.TimeNumpadItem

import java.util.concurrent.TimeUnit

@UnstableApi
@Preview
@Composable
fun TimerFactoryDialog(
    onDismiss: () -> Unit = {},
    onTimeChosen: (Int, Int, Int, SoundData?, Boolean) -> Unit = { _, _, _, _, _ -> },
    defaultHours: Int = 0,
    defaultMinutes: Int = 0,
    defaultSeconds: Int = 0
) {
    val context = LocalContext.current

    val initialInput = remember {
        val totalHours = defaultHours + TimeUnit.MINUTES.toHours(defaultMinutes.toLong()).toInt()
        val totalMinutes = (defaultMinutes % 60 + TimeUnit.SECONDS.toMinutes(defaultSeconds.toLong())).toInt()
        val totalSeconds = defaultSeconds % 60
        "%02d%02d%02d".format(totalHours, totalMinutes, totalSeconds)
    }

    val initialRingtone = remember {
        Preferences.DEFAULT_TIMER_RINGTONE.get(context).let {
            SoundData.fromString(it).getOrNull()
        }
    }

    var showSoundDialog by remember { mutableStateOf(false) }

    var ringtone by remember { mutableStateOf(initialRingtone) }
    var input by remember { mutableStateOf(initialInput) }
    var isVibrate by remember { mutableStateOf(false) }

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
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(id = android.R.string.cancel),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    if (input.toInt() > 0) {
                        onTimeChosen(hours, minutes, seconds, ringtone, isVibrate)
                        onDismiss()
                    }
                }) {
                    Text(
                        text = stringResource(id = R.string.title_start_timer),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .defaultMinSize(minWidth = 350.dp, minHeight = 512.dp)
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(2f)
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { showSoundDialog = true }
                ) {
                    if (ringtone != null) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ringtone),
                            contentDescription = "Ringtone null",
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = ringtone!!.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ringtone_disabled),
                            contentDescription = "Ringtone on",
                            modifier = Modifier
                                .size(30.dp)
                                .alpha(0.33f)
                        )
                        Text(
                            text = stringResource(id = R.string.title_sound_none),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_expand),
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable { isVibrate = !isVibrate }
                ) {
                    AnimatedContent(
                        targetState = isVibrate,
                        label = "VibrateToggle"
                    ) { vibrate ->
                        if (vibrate) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_vibrate),
                                contentDescription = "Vibrate on",
                                modifier = Modifier.size(30.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_vibrate_none),
                                contentDescription = "Vibrate off",
                                modifier = Modifier
                                    .size(30.dp)
                                    .alpha(0.33f)
                            )
                        }
                    }
                    Text(
                        text = stringResource(id = R.string.title_vibrate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .widthIn(min = 350.dp)
    )

    if (showSoundDialog) {
        SoundChooserDialog(
            onDismissRequest = { showSoundDialog = false },
            onSoundChosen = {
                ringtone = it
                showSoundDialog = false
            },
        )
    }
}
