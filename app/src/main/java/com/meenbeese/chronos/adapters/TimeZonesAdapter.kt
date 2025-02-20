package com.meenbeese.chronos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.checkbox.MaterialCheckBox
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.PreferenceData

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import kotlin.math.abs


class TimeZonesAdapter(
    private val timeZones: List<String>
) : RecyclerView.Adapter<TimeZonesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_zone, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeZone = TimeZone.getTimeZone(timeZones[position])
        val offsetMillis = timeZone.rawOffset
        holder.time.text = String.format(
            Locale.getDefault(),
            "GMT%s%02d:%02d",
            if (offsetMillis >= 0) "+" else "",
            TimeUnit.MILLISECONDS.toHours(offsetMillis.toLong()),
            TimeUnit.MILLISECONDS.toMinutes(abs(offsetMillis).toLong()) % TimeUnit.HOURS.toMinutes(1)
        )
        holder.title.text = timeZone.getDisplayName(Locale.getDefault())
        holder.itemView.setOnClickListener { holder.checkBox.toggle() }
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = PreferenceData.TIME_ZONE_ENABLED.getSpecificValue<Any>(
            holder.itemView.context,
            timeZone.id
        ) as Boolean
        holder.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            val timeZone1 = TimeZone.getTimeZone(
                timeZones[holder.bindingAdapterPosition]
            )
            PreferenceData.TIME_ZONE_ENABLED.setValue(
                holder.itemView.context,
                isChecked,
                timeZone1.id
            )
        }
    }

    override fun getItemCount(): Int {
        return timeZones.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val time: TextView = v.findViewById(R.id.time)
        val title: TextView = v.findViewById(R.id.title)
        val checkBox: MaterialCheckBox = v.findViewById(R.id.checkbox)
    }
}
