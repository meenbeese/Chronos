package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.interfaces.SoundChooserListener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Allows the user to select from a set of
 * ringtone sounds (preference is a string
 * that can be recreated into a SoundData
 * object).
 */
class RingtonePreferenceData(
    private val preference: PreferenceData,
    name: Int
) : CustomPreferenceData(name) {

    override fun getValueName(holder: ViewHolder): String {
        val context = holder.context
        val sound = runBlocking { preference.getValue<String>(context) }
        return if (sound?.isNotEmpty() == true) {
            SoundData.fromString(sound)?.name ?: context.getString(R.string.title_sound_none)
        } else {
            context.getString(R.string.title_sound_none)
        }
    }

    override fun onClick(holder: ViewHolder) {
        holder.chronos?.fragmentManager?.let { manager ->
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        preference.setValue(holder.context, sound?.toString())

                        withContext(Dispatchers.Main) {
                            bindViewHolder(holder)
                        }
                    }
                }
            })
            dialog.show(manager, null)
        }
    }
}
