package com.meenbeese.chronos.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Display a progress line, with a given foreground/background
 * color set.
 */
@Composable
fun ProgressLineView(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progressLineAnim"
    )

    val shadowColor = if (backgroundColor.luminance() < 0.5f) {
        lineColor.copy(alpha = 0.3f)
    } else {
        lineColor.copy(alpha = 0.2f).copy(alpha = 0.6f)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect(
            color = backgroundColor,
            size = size,
        )

        drawRect(
            color = shadowColor,
            size = size.copy(width = canvasWidth * animatedProgress, height = canvasHeight),
            topLeft = Offset(0f, 2f)
        )

        drawRect(
            color = lineColor,
            size = size.copy(width = canvasWidth * animatedProgress),
        )
    }
}
