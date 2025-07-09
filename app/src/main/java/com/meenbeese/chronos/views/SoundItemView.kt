package com.meenbeese.chronos.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.meenbeese.chronos.R

@Composable
fun SoundItemView(
    title: String,
    isPlaying: Boolean,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playIcon = painterResource(id = R.drawable.ic_play)
    val pauseIcon = painterResource(id = R.drawable.ic_pause)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = if (isPlaying) pauseIcon else playIcon,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier
                .size(42.dp)
                .padding(end = 8.dp)
                .clickable { onIconClick() }
        )
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
