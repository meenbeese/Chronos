package com.meenbeese.chronos.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Display a progress line, with a given foreground/background
 * color set.
 */
@Composable
fun ProgressLineView(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    backgroundColor: Color = Color.LightGray,
    lineColor: Color = Color.DarkGray
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progressLineAnim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            color = backgroundColor,
            size = size,
            style = Fill
        )

        drawRect(
            color = lineColor,
            size = size.copy(width = canvasWidth * animatedProgress),
            style = Fill
        )
    }
}
