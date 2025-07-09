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
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.interfaces.SoundChooserListener
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
        SoundChooserDialogFragmentHost(
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
            }
        )
    }
}

@Composable
@UnstableApi
fun SoundChooserDialogFragmentHost(
    onDismissRequest: () -> Unit,
    onSoundChosen: (SoundData) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var dialogShown by remember { mutableStateOf(false) }

    if (!dialogShown) {
        LaunchedEffect(Unit) {
            activity?.supportFragmentManager?.let { fm ->
                val dialog = SoundChooserDialog()
                dialog.setListener(object : SoundChooserListener {
                    override fun onSoundChosen(sound: SoundData?) {
                        sound?.let { onSoundChosen(it) }
                        onDismissRequest()
                        dialog.dismiss()
                    }
                })

                dialog.show(fm, "soundChooserDialog")
            }
        }
    }
}
