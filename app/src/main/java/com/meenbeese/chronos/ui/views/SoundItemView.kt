package com.meenbeese.chronos.ui.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    modifier: Modifier = Modifier
) {
    val playIcon = Icons.Sharp.PlayArrow
    val pauseIcon = Icons.Sharp.Pause

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 8.dp, end = 16.dp),
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
}
