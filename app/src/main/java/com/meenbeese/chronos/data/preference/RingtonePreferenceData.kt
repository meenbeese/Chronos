package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.dialogs.SoundChooserDialog
import com.meenbeese.chronos.interfaces.SoundChooserListener


/**
 * Allows the user to select from a set of
 * ringtone sounds (preference is a string
 * that can be recreated into a SoundData
 * object).
 */
class RingtonePreferenceData(private val preference: PreferenceData, name: Int) : CustomPreferenceData(name) {
    override fun getValueName(holder: ViewHolder): String {
        return preference.getValue(holder.context, "")?.let{ sound ->
            if (sound.isNotEmpty())
                SoundData.fromString(sound)?.name ?: holder.context.getString(R.string.title_sound_none)
            else null
        } ?: holder.context.getString(R.string.title_sound_none)
    }

    override fun onClick(holder: ViewHolder) {
        holder.chronos?.fragmentManager?.let { manager ->
            val dialog = SoundChooserDialog()
            dialog.setListener(object : SoundChooserListener {
                override fun onSoundChosen(sound: SoundData?) {
                    preference.setValue(holder.context, sound?.toString())
                    bindViewHolder(holder)
                }
            })
            dialog.show(manager, null)
        }
    }
}
