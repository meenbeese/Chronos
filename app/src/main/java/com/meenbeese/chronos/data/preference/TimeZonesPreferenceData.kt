package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.dialogs.TimeZoneChooserDialog

import java.util.*


/**
 * A preference item allowing the user to select
 * from multiple time zones (preference is a boolean,
 * should have a parameter for the zone id).
 */
class TimeZonesPreferenceData(private val preference: PreferenceData, title: Int) : CustomPreferenceData(title) {

    override fun getValueName(holder: ViewHolder): String {
        var i = 0
        for (id in TimeZone.getAvailableIDs()) {
            if (preference.getSpecificValue(holder.context, id))
                i++
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
