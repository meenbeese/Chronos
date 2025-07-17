package com.meenbeese.chronos.ui.views.sound

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.ext.dataStore
import com.meenbeese.chronos.ui.screens.FileChooserScreen
import com.meenbeese.chronos.ui.screens.FileChooserType
import com.meenbeese.chronos.ui.views.SoundItemView
import com.meenbeese.chronos.utils.AudioUtils

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val SEPARATOR = ":ChronosFileSound:"

@UnstableApi
@Composable
fun FileSoundChooserView(
    onSoundChosen: (SoundData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioUtils = AudioUtils(context)
    val prefKey = remember { stringSetPreferencesKey("previousFiles") }

    var sounds by remember { mutableStateOf<List<SoundData>>(emptyList()) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var showFileChooser by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val preferences = context.dataStore.data.first()
        val fileStrings = preferences[prefKey] ?: emptySet()
        sounds = fileStrings.toList()
            .sortedBy {
                it.split(SEPARATOR).firstOrNull()?.toIntOrNull() ?: 0
            }
            .mapNotNull { str ->
                val parts = str.split(SEPARATOR)
                if (parts.size == 3) SoundData(parts[1], SoundData.TYPE_RINGTONE, parts[2]) else null
            }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Button(
            onClick = {
                showFileChooser = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Audio File")
        }

        Text(
            text = stringResource(id = R.string.desc_audio_file),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(sounds) { sound ->
                val isPlaying = currentlyPlayingUrl == sound.url

                SoundItemView(
                    title = sound.name,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
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

    if (showFileChooser) {
        FileChooserScreen(
            type = FileChooserType.AUDIO,
            preference = null,
            onFileChosen = { name, uri ->
                val sound = SoundData(name, SoundData.TYPE_RINGTONE, uri)
                onSoundChosen(sound)

                // Insert new file at top
                sounds = (sounds - sound).toMutableList().apply { add(0, sound) }

                // Persist to DataStore
                scope.launch {
                    val entries = sounds.mapIndexed { index, s ->
                        "$index$SEPARATOR${s.name}$SEPARATOR${s.url}"
                    }.toSet()
                    context.dataStore.edit { it[prefKey] = entries }
                }

                showFileChooser = false
            },
            onDismiss = {
                showFileChooser = false
            }
        )
    }
}
