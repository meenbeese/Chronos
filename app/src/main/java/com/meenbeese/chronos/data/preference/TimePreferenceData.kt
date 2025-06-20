package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.dialogs.TimeChooserDialogLegacy
import com.meenbeese.chronos.utils.FormatUtils

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.concurrent.TimeUnit

/**
 * A preference item that holds / displays a time value.
 */
class TimePreferenceData(
    private val preference: PreferenceEntry.LongPref,
    name: Int
) : CustomPreferenceData(name) {
    override fun getValueName(holder: ViewHolder): String {
        return FormatUtils.formatMillis(preference.get(holder.context)).run {
            substring(0, length - 3)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onClick(holder: ViewHolder) {
        val dialog = run {
            var seconds = TimeUnit.MILLISECONDS.toSeconds(preference.get(holder.context)).toInt()
            var minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
            val hours = TimeUnit.MINUTES.toHours(minutes.toLong()).toInt()
            minutes %= TimeUnit.HOURS.toMinutes(1).toInt()
            seconds %= TimeUnit.MINUTES.toSeconds(1).toInt()

            TimeChooserDialogLegacy(holder.context).apply { setDefault(hours, minutes, seconds) }
        }

        dialog.setListener(object : TimeChooserDialogLegacy.OnTimeChosenListener {
            override fun onTimeChosen(hours: Int, minutes: Int, seconds: Int) {
                val totalSeconds = seconds + TimeUnit.HOURS.toSeconds(hours.toLong()).toInt() + TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt()

                GlobalScope.launch {
                    preference.set(holder.context, TimeUnit.SECONDS.toMillis(totalSeconds.toLong()))
                }

                bindViewHolder(holder)
            }
        })
        dialog.show()
    }
}
