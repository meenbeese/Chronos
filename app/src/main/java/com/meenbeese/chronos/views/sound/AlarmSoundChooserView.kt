package com.meenbeese.chronos.views.sound

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ext.loadRingtones
import com.meenbeese.chronos.utils.AudioUtils
import com.meenbeese.chronos.views.SoundItemView

@UnstableApi
@Composable
fun AlarmSoundChooserView(
    onSoundChosen: (SoundData) -> Unit
) {
    val context = LocalContext.current
    val audioUtils = AudioUtils(context)
    val sounds = remember { loadRingtones(context) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        items(sounds) { sound ->
            val isPlaying = currentlyPlayingUrl == sound.url

            SoundItemView(
                title = sound.name,
                isPlaying = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
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
