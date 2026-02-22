package com.meenbeese.chronos.ui.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SoundItemView(
    title: String,
    isPlaying: Boolean,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = 0f
) {
    val playIcon = Icons.Sharp.PlayArrow
    val pauseIcon = Icons.Sharp.Pause

    val animatedProgress = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "SoundProgress"
    )

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Crossfade(
                targetState = isPlaying,
                label = "PlayPauseCrossfade"
            ) { playing ->
                Icon(
                    imageVector = if (playing) pauseIcon else playIcon,
                    contentDescription = if (playing) "Pause" else "Play",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 8.dp)
                        .clickable { onIconClick() }
                )
            }

            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isPlaying) {
            Slider(
                value = animatedProgress.value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                enabled = false,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
        }
    }
}
