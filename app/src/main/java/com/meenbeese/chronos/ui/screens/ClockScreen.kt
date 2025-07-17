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

import java.util.TimeZone

@Composable
fun ClockScreen(
    timezoneId: String,
    onClockTap: () -> Unit,
    getTextColor: suspend () -> Int
) {
    val timezone = TimeZone.getTimeZone(timezoneId)

    var timezoneLabel by remember {
        mutableStateOf(
            if (timezone.id != TimeZone.getDefault().id) {
                "${timezone.id.replace("_", " ")}\n${timezone.displayName}"
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
