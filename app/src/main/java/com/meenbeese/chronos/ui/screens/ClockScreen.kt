package com.meenbeese.chronos.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.meenbeese.chronos.ui.views.DigitalClockView

import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt

import kotlin.math.abs
import kotlin.time.Clock

@Composable
fun ClockScreen(
    timezoneId: String,
    onClockTap: () -> Unit,
    getTextColor: suspend () -> Int
) {
    val timezone = remember(timezoneId) { TimeZone.of(timezoneId) }

    val gmtOffset = remember(timezoneId) {
        val totalSeconds = timezone.offsetAt(Clock.System.now()).totalSeconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val sign = if (totalSeconds >= 0) "+" else "-"
        "GMT$sign%02d:%02d".format(abs(hours), abs(minutes))
    }

    var timezoneLabel by remember {
        mutableStateOf(
            if (timezone.id != TimeZone.currentSystemDefault().id) {
                "${timezone.id.replace("_", " ")}\n$gmtOffset"
            } else ""
        )
    }

    var textColor by remember { mutableIntStateOf(android.graphics.Color.DKGRAY) }

    LaunchedEffect(Unit) {
        textColor = getTextColor()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        DigitalClockView(
            modifier = Modifier.fillMaxSize(),
            timezoneId = timezoneId,
            onClick = onClockTap
        )

        if (timezoneLabel.isNotBlank()) {
            Text(
                text = timezoneLabel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(72.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(textColor),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
