package com.meenbeese.chronos.data.preference

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.screens.FileChooserScreen
import com.meenbeese.chronos.screens.FileChooserType
import com.meenbeese.chronos.views.PreferenceItem

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Allows the user to select from a set of
 * ringtone sounds (preference is a string
 * that can be recreated into a SoundData
 * object).
 */
@Composable
@UnstableApi
fun RingtonePreference(
    preference: PreferenceEntry.StringPref,
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var soundName by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showFileChooser by remember { mutableStateOf(false) }
    var fileChooserCallback by remember { mutableStateOf<(String, String) -> Unit>({ _, _ -> }) }

    // Load sound name on composition and when preference changes
    LaunchedEffect(preference) {
        val soundStr = preference.get(context)
        soundName = if (soundStr.isNotEmpty()) {
            SoundData.fromString(soundStr)
                .map { it.name }
                .getOrElse(context.getString(R.string.title_sound_none))
        } else {
            context.getString(R.string.title_sound_none)
        }
    }

    PreferenceItem(
        title = stringResource(id = titleRes),
        description = soundName,
        onClick = { showDialog = true },
        modifier = modifier
    )

    if (showDialog) {
        SoundChooserDialog(
            onDismissRequest = { showDialog = false },
            onSoundChosen = { sound ->
                // Save selection asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    preference.set(context, sound.toString())
                    withContext(Dispatchers.Main) {
                        soundName = sound.name
                        showDialog = false
                    }
                }
            },
            onRequestFileChooser = { callback ->
                fileChooserCallback = callback
                showFileChooser = true
            }
        )
    }

    if (showFileChooser) {
        FileChooserScreen(
            type = FileChooserType.AUDIO,
            preference = preference,
            onFileChosen = { name, uri ->
                fileChooserCallback(name, uri)
                showFileChooser = false
                showDialog = false
            },
            onDismiss = {
                showFileChooser = false
            }
        )
    }
}
