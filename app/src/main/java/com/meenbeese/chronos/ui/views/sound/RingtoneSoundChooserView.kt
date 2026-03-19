package com.meenbeese.chronos.ui.views.sound

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ui.views.SoundItemView
import com.meenbeese.chronos.utils.AudioManager

@UnstableApi
@Composable
fun RingtoneSoundChooserView(
    sounds: List<SoundData>,
    onSoundChosen: (SoundData) -> Unit
) {
    val context = LocalContext.current
    val audioUtils = AudioManager(context)
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        items(sounds) { sound ->
            val isPlaying = currentlyPlayingUrl == sound.url
            var progress by remember { mutableStateOf(0f) }
            var currentMillis by remember { mutableStateOf(0L) }
            var totalMillis by remember { mutableStateOf(0L) }

            if (isPlaying) {
                LaunchedEffect(currentlyPlayingUrl) {
                    while (true) {
                        val pos = audioUtils.getCurrentPosition(sound.url)
                        val dur = audioUtils.getDuration(sound.url)
                        progress = if (dur > 0L) pos.toFloat() / dur.toFloat() else 0f
                        currentMillis = pos
                        totalMillis = dur
                        kotlinx.coroutines.delay(100L)
                    }
                }
            } else {
                progress = 0f
                currentMillis = 0L
                totalMillis = 0L
            }

            SoundItemView(
                title = sound.name,
                isPlaying = isPlaying,
                progress = progress,
                currentMillis = currentMillis,
                totalMillis = totalMillis,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onSoundChosen(sound) },
                onIconClick = {
                    if (isPlaying) {
                        audioUtils.stopCurrentSound()
                        currentlyPlayingUrl = null
                    } else {
                        audioUtils.stopCurrentSound()
                        audioUtils.playStream(sound.url, sound.type, null)
                        currentlyPlayingUrl = sound.url
                    }
                }
            )
        }
    }
}
