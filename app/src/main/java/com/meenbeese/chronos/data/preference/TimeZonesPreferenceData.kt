package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.dialogs.TimeZoneChooserDialog

import kotlinx.coroutines.runBlocking

import java.util.Locale
import java.util.TimeZone

/**
 * A preference item allowing the user to select
 * from multiple time zones (preference is a boolean,
 * should have a parameter for the zone id).
 */
class TimeZonesPreferenceData(
    private val preference: PreferenceData,
    title: Int
) : CustomPreferenceData(title) {

    override fun getValueName(holder: ViewHolder): String {
        var i = 0
        runBlocking {
            for (id in TimeZone.getAvailableIDs()) {
                if (preference.getValue(holder.context)) {
                    i++
                }
            }
        }

        return String.format(Locale.getDefault(), holder.context.getString(R.string.msg_time_zones_selected), i)
    }

    override fun onClick(holder: ViewHolder) {
        TimeZoneChooserDialog(holder.context).apply {
            setOnDismissListener { bindViewHolder(holder) }
            show()
        }
    }
}
