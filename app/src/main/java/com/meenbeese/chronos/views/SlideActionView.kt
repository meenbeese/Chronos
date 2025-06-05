package com.meenbeese.chronos.views

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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

import kotlinx.coroutines.delay
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
    val positionX = remember { mutableFloatStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }
    val inLeftSnapZone = remember { mutableStateOf(false) }
    val inRightSnapZone = remember { mutableStateOf(false) }

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(
        modifier = modifier
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        positionX.floatValue = offset.x
                        scope.launch {
                            selected.snapTo(1f)
                        }
                    },
                    onDrag = { change, _ ->
                        val newX = change.position.x.coerceIn(0f, size.width.toFloat())
                        val snapEdgePaddingPx = with(density) { (32.dp + 20.dp).toPx() }
                        positionX.floatValue = newX
                        inLeftSnapZone.value = newX <= snapEdgePaddingPx
                        inRightSnapZone.value = newX >= size.width - snapEdgePaddingPx
                        change.consume()
                    },
                    onDragEnd = {
                        scope.launch {
                            val snapEdgePaddingPx = with(density) { (32.dp + 20.dp).toPx() }
                            val width = size.width.toFloat()
                            val center = width / 2f

                            val leftSnapZone = snapEdgePaddingPx
                            val rightSnapZone = width - snapEdgePaddingPx

                            when {
                                positionX.floatValue <= leftSnapZone -> {
                                    // Trigger action, then return to center
                                    onSlideLeft()
                                    animateToPosition(positionX.floatValue, center, 16L, 150) {
                                        positionX.floatValue = it
                                    }
                                }
                                positionX.floatValue >= rightSnapZone -> {
                                    // Trigger action, then return to center
                                    onSlideRight()
                                    animateToPosition(positionX.floatValue, center, 16L, 150) {
                                        positionX.floatValue = it
                                    }
                                }
                                else -> {
                                    // Just return to center
                                    animateToPosition(positionX.floatValue, center, 16L, 150) {
                                        positionX.floatValue = it
                                    }
                                }
                            }

                            selected.snapTo(0f)
                            inLeftSnapZone.value = false
                            inRightSnapZone.value = false
                        }
                    }
                )
            }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }

        LaunchedEffect(widthPx) {
            if (positionX.floatValue == 0f) {
                positionX.floatValue = widthPx / 2f
            }
        }

        val centerX = with(density) { positionX.floatValue.toDp() }
        val radius = with(density) { (12 + (20 * selected.value)).dp }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
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
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = false, radius = radius),
                    onClick = {} // ignored
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (inLeftSnapZone.value) iconColor.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (leftIcon != null) {
                    Icon(
                        painter = leftIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (inRightSnapZone.value) iconColor.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (rightIcon != null) {
                    Icon(
                        painter = rightIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }
        }
    }
}

suspend fun animateToPosition(
    from: Float,
    to: Float,
    frameDelay: Long,
    duration: Int,
    onUpdate: (Float) -> Unit
) {
    val frameCount = duration / frameDelay.toInt()
    val delta = to - from
    for (i in 1..frameCount) {
        val progress = i / frameCount.toFloat()
        val value = from + delta * progress
        onUpdate(value)
        delay(frameDelay)
    }
    onUpdate(to)
}
