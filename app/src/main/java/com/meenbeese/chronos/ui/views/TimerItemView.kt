package com.meenbeese.chronos.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.delay

@Composable
fun TimerItemView(
    timer: TimerData,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
    progressContent: @Composable (progress: Float) -> Unit
) {
    var remainingMillis by remember { mutableStateOf(timer.remainingMillis) }

    LaunchedEffect(timer) {
        while (timer.isSet && remainingMillis > 0L) {
            remainingMillis = timer.remainingMillis
            delay(1000)
        }
    }

    val progress = ((timer.duration - remainingMillis).toFloat() / timer.duration).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "timerProgressAnim"
    )

    val timeText = FormatUtils.formatMillis(remainingMillis).dropLast(2)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        progressContent(animatedProgress)

        var rowSize by remember { mutableStateOf(IntSize.Zero) }
        val animatedProgress = animatedProgress * constraints.maxWidth

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 12.dp)
                .onGloballyPositioned { rowSize = it.size },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textOffset = 0f
            val iconOffset = rowSize.width.toFloat() - 40.dp.value

            val textColor = if (animatedProgress >= textOffset) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            val iconColor = if (animatedProgress >= iconOffset) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = timeText,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            IconButton(
                onClick = onStopClick,
                modifier = Modifier
                    .size(40.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Stop",
                    tint = iconColor
                )
            }
        }
    }
}
