package com.meenbeese.chronos.views

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayCircleView(
    text: String,
    isChecked: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(isChecked) }

    val transition = updateTransition(targetState = isChecked, label = "DayCircleTransition")
    val scale by transition.animateFloat(
        label = "BouncyScale",
        transitionSpec = {
            spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        }
    ) { checked -> if (checked) 1f else 0f }

    val bgColor = if (scale > 0.5f) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (scale > 0.5f) Color.White else Color.Black

    val fontSize = 14.sp
    val textMeasurer = rememberTextMeasurer()
    val sizePx = with(LocalDensity.current) { size.toPx() }

    Box(
        modifier = modifier
            .size(size)
            .clickable {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = bgColor,
                radius = sizePx / 2 * scale
            )

            val textLayoutResult = textMeasurer.measure(
                text = text,
                style = TextStyle(
                    color = textColor,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            )

            drawIntoCanvas {
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height
                val x = (sizePx - textWidth) / 2
                val y = (sizePx - textHeight) / 2

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(x, y)
                )
            }
        }
    }
}
