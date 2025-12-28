package com.meenbeese.chronos.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.utils.FormatUtils

import java.util.concurrent.TimeUnit

@Composable
fun AlarmItemView(
    alarm: AlarmData,
    onAlarmUpdated: (AlarmData) -> Unit,
    onTimeClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRepeatToggle: (Boolean) -> Unit,
    onDayToggle: (Int, Boolean) -> Unit,
    onRingtoneClick: () -> Unit,
    onVibrateToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    onExpandClick: () -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nextAlarmTime = alarm.getNext()?.timeInMillis
    val now = System.currentTimeMillis()

    var name by remember { mutableStateOf(alarm.name.orEmpty()) }
    var days by remember { mutableStateOf(alarm.days.toMutableList()) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ExpandIconRotation"
    )

    val alarmText = if (alarm.isEnabled && nextAlarmTime != null && nextAlarmTime > now) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(nextAlarmTime - now)
        "Next alarm: ${FormatUtils.formatUnit(context, minutes.toInt())}"
    } else null

    val dayLabels = listOf(
        stringResource(R.string.day_sunday_abbr),
        stringResource(R.string.day_monday_abbr),
        stringResource(R.string.day_tuesday_abbr),
        stringResource(R.string.day_wednesday_abbr),
        stringResource(R.string.day_thursday_abbr),
        stringResource(R.string.day_friday_abbr),
        stringResource(R.string.day_saturday_abbr)
    )

    Column(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(if (isExpanded) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FormatUtils.formatShort(context, alarm.time.time),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTimeClick)
            )

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggleEnabled,
                modifier = Modifier
                    .padding(start = 12.dp)
            )
        }

        if (alarmText != null) {
            Text(
                text = alarmText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Checkbox(
                        checked = days.any { it },
                        onCheckedChange = { isChecked ->
                            days = List(7) { isChecked }.toMutableList()
                            onRepeatToggle(isChecked)
                        }
                    )
                    Text(
                        text = stringResource(R.string.title_repeat),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                if (days.any { it }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayLabels.forEachIndexed { i, label ->
                            DayCircleView(
                                text = label,
                                isChecked = days[i],
                                onCheckedChange = {
                                    days[i] = it
                                    days = days.toMutableList()
                                    onDayToggle(i, it)
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconToggleView(
                        iconRes = if (alarm.sound != null) R.drawable.ic_ringtone else R.drawable.ic_ringtone_disabled,
                        text = alarm.sound?.name ?: stringResource(R.string.title_none),
                        onClick = onRingtoneClick,
                        enabled = alarm.sound != null
                    )

                    IconToggleView(
                        iconRes = if (alarm.isVibrate) R.drawable.ic_vibrate else R.drawable.ic_vibrate_none,
                        text = stringResource(R.string.title_vibrate),
                        onClick = onVibrateToggle,
                        enabled = alarm.isVibrate
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_expand),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(30.dp)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
                    .clickable(onClick = onExpandClick)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (name.isEmpty()) {
                        Text(
                            text = "Alarm Name",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    BasicTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            alarm.name = it
                            onAlarmUpdated(alarm)
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (isExpanded) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.title_delete),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onDeleteClick)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    val iconAlpha = { enabled: Boolean -> if (enabled) 1f else 0.33f }

                    Icon(
                        painter = painterResource(R.drawable.ic_repeat),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(iconAlpha(alarm.isRepeat()))
                            .clickable(onClick = onExpandClick)
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_sound),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(iconAlpha(alarm.sound != null))
                            .clickable(onClick = onExpandClick)
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_vibrate),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(iconAlpha(alarm.isVibrate))
                            .clickable(onClick = onExpandClick)
                    )
                }
            }
        }
    }
}
