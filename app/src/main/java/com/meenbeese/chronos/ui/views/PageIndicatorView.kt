package com.meenbeese.chronos.ui.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicatorView(
    currentPage: Int,
    pageOffset: Float,
    pageCount: Int,
    modifier: Modifier = Modifier,
    dotRadius: Dp = 4.dp,
    spacing: Dp = 8.dp,
    primaryColor: Color = Color.DarkGray,
    secondaryColor: Color = Color.LightGray
) {
    val dotRadiusPx = with(LocalDensity.current) { dotRadius.toPx() }
    val spacingPx = with(LocalDensity.current) { spacing.toPx() }

    val indicatorHeight = dotRadius * 2
    val indicatorWidth = (dotRadius * 2 + spacing) * pageCount - spacing

    Canvas(
        modifier = modifier
            .width(indicatorWidth)
            .height(indicatorHeight)
            .clipToBounds()
    ) {
        val centerY = size.height / 2f

        // Draw inactive dots
        for (i in 0 until pageCount) {
            val x = i * (dotRadiusPx * 2 + spacingPx) + dotRadiusPx
            drawCircle(
                color = secondaryColor,
                radius = dotRadiusPx,
                center = Offset(x, centerY)
            )
        }

        val currentX = currentPage * (dotRadiusPx * 2 + spacingPx) + dotRadiusPx
        val nextX = (currentPage + 1).coerceAtMost(pageCount - 1) * (dotRadiusPx * 2 + spacingPx) + dotRadiusPx
        val animatedX = currentX + (nextX - currentX) * pageOffset

        // Draw active indicator
        drawCircle(
            color = primaryColor,
            radius = dotRadiusPx,
            center = Offset(animatedX, centerY)
        )
    }
}
