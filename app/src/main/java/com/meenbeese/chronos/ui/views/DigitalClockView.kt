package com.meenbeese.chronos.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.delay

import java.util.Calendar
import java.util.TimeZone

@Composable
fun DigitalClockView(
    modifier: Modifier = Modifier,
    timezoneId: String = TimeZone.getDefault().id,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val timezone = remember(timezoneId) { TimeZone.getTimeZone(timezoneId) }

    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(timezoneId) {
        while (true) {
            val defaultZone = TimeZone.getDefault()
            TimeZone.setDefault(timezone)
            val formatted = FormatUtils.format(context, Calendar.getInstance().time)
            currentTime = formatted.ifEmpty { "00:00:00" }
            TimeZone.setDefault(defaultZone)
            delay(1000)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = currentTime,
            fontSize = 64.sp,
            fontFamily = FontFamily.Default,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
    }
}
