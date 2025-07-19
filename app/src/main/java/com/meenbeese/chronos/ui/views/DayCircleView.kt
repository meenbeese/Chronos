package com.meenbeese.chronos.ui.views

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .clickable {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
