package com.meenbeese.chronos.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PreferenceSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    topPadding: Dp = 16.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val squiggleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .drawWithCache {
                    if (size.width <= 0f || size.height <= 0f) {
                        return@drawWithCache onDrawBehind { }
                    }

                    val amplitude = size.height * 0.22f
                    val midY = size.height / 2f
                    val wavelength = (size.width / 6f).coerceAtLeast(36f)
                    val step = 2f

                    val path = Path().apply {
                        moveTo(0f, midY)
                        var x = 0f
                        while (x <= size.width) {
                            val y = midY + amplitude * sin(2.0 * PI * (x / wavelength)).toFloat()
                            lineTo(x, y)
                            x += step
                        }
                    }

                    onDrawBehind {
                        drawPath(
                            path = path,
                            color = squiggleColor,
                            style = Stroke(
                                width = 4f,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
        )
    }
}
