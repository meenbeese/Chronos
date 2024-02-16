package me.jfenn.alarmio.data.preference

import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.dialogs.SoundChooserDialog
import me.jfenn.alarmio.interfaces.SoundChooserListener


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
        holder.alarmio?.fragmentManager?.let { manager ->
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
