package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.dialogs.TimeZoneChooserDialog

import kotlinx.coroutines.runBlocking

import java.util.Locale

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
        val rawCsv = PreferenceData.TIME_ZONES.getValue<String>(holder.context)
        val selectedZones = rawCsv.split(",").filter { it.isNotBlank() }
        val count = selectedZones.size

        return String.format(
            Locale.getDefault(),
            holder.context.getString(R.string.msg_time_zones_selected),
            count
        )
    }

    override fun onClick(holder: ViewHolder) {
        val rawCsv = PreferenceData.TIME_ZONES.getValue<String>(holder.context)
        val selectedZones = rawCsv.split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()

        TimeZoneChooserDialog(holder.context, selectedZones) { updatedSelection ->
            val csv = updatedSelection.joinToString(",")
            runBlocking {
                PreferenceData.TIME_ZONES.setValue(holder.context, csv)
                PreferenceData.TIME_ZONE_ENABLED.setValue(holder.context, updatedSelection.isNotEmpty())
            }

            (holder.context as? MainActivity)?.refreshClockFragments()

            bindViewHolder(holder)
        }.apply {
            setOnDismissListener { bindViewHolder(holder) }
            show()
        }
    }
}
