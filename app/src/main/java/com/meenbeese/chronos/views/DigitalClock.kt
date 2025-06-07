package com.meenbeese.chronos.views

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext

import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.delay

import java.util.Calendar
import java.util.TimeZone

@Composable
fun DigitalClock(
    modifier: Modifier = Modifier,
    timezoneId: String = TimeZone.getDefault().id
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

    val bounds = remember {
        Rect().also { rect ->
            val tempPaint = Paint().apply {
                textSize = 48f
            }
            tempPaint.getTextBounds("00:00:00", 0, 8, rect)
        }
    }

    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvas = drawContext.canvas.nativeCanvas

            val textSize = 48f * size.width / (bounds.width() * 2)
            paint.textSize = textSize

            val x = size.width / 2
            val y = (size.height - paint.ascent()) / 2
            canvas.drawText(currentTime, x, y, paint)
        }
    }
}
