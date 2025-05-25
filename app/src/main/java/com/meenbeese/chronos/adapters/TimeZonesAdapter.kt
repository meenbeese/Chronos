package com.meenbeese.chronos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import com.meenbeese.chronos.R

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import kotlin.math.abs

class TimeZonesAdapter(
    private val timeZones: List<String>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<TimeZonesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_time_zone, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeZoneId = timeZones[position]
        val timeZone = TimeZone.getTimeZone(timeZoneId)
        val offsetMillis = timeZone.rawOffset

        // Format offset as GMT+/-hh:mm
        holder.time.text = String.format(
            Locale.getDefault(),
            "GMT%s%02d:%02d",
            if (offsetMillis >= 0) "+" else "-",
            TimeUnit.MILLISECONDS.toHours(abs(offsetMillis.toLong())),
            TimeUnit.MILLISECONDS.toMinutes(abs(offsetMillis.toLong())) % TimeUnit.HOURS.toMinutes(1)
        )

        holder.title.text = timeZone.getDisplayName(Locale.getDefault())
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selected.contains(timeZoneId)

        holder.itemView.setOnClickListener {
            holder.checkBox.toggle()
        }

        holder.checkBox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                selected.add(timeZoneId)
            } else {
                selected.remove(timeZoneId)
            }
        }
    }

    override fun getItemCount(): Int {
        return timeZones.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val time: MaterialTextView = v.findViewById(R.id.time)
        val title: MaterialTextView = v.findViewById(R.id.title)
        val checkBox: MaterialCheckBox = v.findViewById(R.id.checkbox)
    }
}
