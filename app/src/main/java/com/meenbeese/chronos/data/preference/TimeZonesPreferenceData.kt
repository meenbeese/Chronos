package com.meenbeese.chronos.data.preference

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.PreferenceEntry
import com.meenbeese.chronos.dialogs.TimeZoneChooserDialog

import kotlinx.coroutines.runBlocking

import java.util.Locale

/**
 * A preference item allowing the user to select
 * from multiple time zones (preference is a boolean,
 * should have a parameter for the zone id).
 */
class TimeZonesPreferenceData(
    private val preference: PreferenceEntry.BooleanPref,
    title: Int
) : CustomPreferenceData(title) {

    override fun getValueName(holder: ViewHolder): String {
        val rawCsv = Preferences.TIME_ZONES.get(holder.context)
        val selectedZones = rawCsv.split(",").filter { it.isNotBlank() }
        val count = selectedZones.size

        return String.format(
            Locale.getDefault(),
            holder.context.getString(R.string.msg_time_zones_selected),
            count
        )
    }

    override fun onClick(holder: ViewHolder) {
        val rawCsv = Preferences.TIME_ZONES.get(holder.context)
        val selectedZones = rawCsv.split(",")
            .filter { it.isNotBlank() }
            .toMutableSet()

        TimeZoneChooserDialog(holder.context, selectedZones) { updatedSelection ->
            val csv = updatedSelection.joinToString(",")
            runBlocking {
                Preferences.TIME_ZONES.set(holder.context, csv)
                Preferences.TIME_ZONE_ENABLED.set(holder.context, updatedSelection.isNotEmpty())
            }

            (holder.context as? MainActivity)?.refreshClockFragments()

            bindViewHolder(holder)
        }.apply {
            setOnDismissListener { bindViewHolder(holder) }
            show()
        }
    }
}
