package com.meenbeese.chronos.views

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch

@Composable
fun SlideActionView(
    modifier: Modifier = Modifier,
    handleColor: Color = Color.Gray,
    outlineColor: Color = Color.Gray,
    iconColor: Color = Color.Gray,
    leftIcon: Painter? = null,
    rightIcon: Painter? = null,
    onSlideLeft: () -> Unit,
    onSlideRight: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val selected = remember { Animatable(0f) }
    val positionX = remember { mutableFloatStateOf(Float.NaN) }

    Box(
        modifier = modifier
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        positionX.floatValue = it.x
                        scope.launch {
                            selected.snapTo(1f)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            selected.snapTo(0f)
                            val width = size.width
                            val leftTrigger = width * 0.25f
                            val rightTrigger = width * 0.75f
                            when {
                                positionX.floatValue <= leftTrigger -> onSlideLeft()
                                positionX.floatValue >= rightTrigger -> onSlideRight()
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        val newX = change.position.x.coerceIn(0f, size.width.toFloat())
                        positionX.floatValue = newX
                        change.consume()
                    }
                )
            }
    ) {
        val centerX = with(LocalDensity.current) { positionX.floatValue.toDp() }
        val radius = with(LocalDensity.current) { (12 + (20 * selected.value)).dp }

        if (positionX.floatValue.isFinite()) {
            Box(
                modifier = Modifier
                    .offset(x = centerX - radius)
                    .size(radius * 2)
                    .drawBehind {
                        drawCircle(
                            color = outlineColor,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    .clip(CircleShape)
                    .background(handleColor.copy(alpha = 0.6f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (leftIcon != null) {
                Icon(
                    painter = leftIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            } else Spacer(modifier = Modifier.size(24.dp))

            if (rightIcon != null) {
                Icon(
                    painter = rightIcon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            } else Spacer(modifier = Modifier.size(24.dp))
        }
    }
}
