package com.meenbeese.chronos.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import kotlin.math.cos
import kotlin.math.sin

/**
 * Display a progress circle with text in
 * the center.
 */
@Composable
fun ProgressTextView(
    text: String,
    progress: Float,
    maxProgress: Float,
    modifier: Modifier = Modifier,
    referenceProgress: Float? = null,
    animate: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progressAnim"
    )

    val strokeWidth = 10.dp
    val color = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val dotColor = MaterialTheme.colorScheme.inversePrimary

    val progressFraction = (if (animate) animatedProgress else progress) / maxProgress.coerceAtLeast(1f)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val radius = size.minDimension / 2 - strokePx
            val center = Offset(size.width / 2, size.height / 2)
            val arcRect = Rect(
                left = strokePx,
                top = strokePx,
                right = size.width - strokePx,
                bottom = size.height - strokePx
            )

            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokePx),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progressFraction * 360f,
                useCenter = false,
                style = Stroke(strokePx, cap = StrokeCap.Round),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            referenceProgress?.let {
                if (maxProgress > 0f) {
                    val angleRad = Math.toRadians((it / maxProgress * 360 - 90).toDouble())
                    val dotX = center.x + cos(angleRad).toFloat() * radius
                    val dotY = center.y + sin(angleRad).toFloat() * radius
                    drawCircle(
                        color = dotColor,
                        radius = strokePx / 2f,
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }
}
