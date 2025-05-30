package com.meenbeese.chronos.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton

import androidx.recyclerview.widget.RecyclerView

import com.meenbeese.chronos.databinding.ItemTimeZoneBinding

import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import kotlin.math.abs

class TimeZonesAdapter(
    private val timeZones: List<String>,
    private val selected: MutableSet<String>
) : RecyclerView.Adapter<TimeZonesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimeZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeZoneId = timeZones[position]
        val timeZone = TimeZone.getTimeZone(timeZoneId)
        val offsetMillis = timeZone.rawOffset

        // Format offset as GMT+/-hh:mm
        holder.binding.time.text = String.format(
            Locale.getDefault(),
            "GMT%s%02d:%02d",
            if (offsetMillis >= 0) "+" else "-",
            TimeUnit.MILLISECONDS.toHours(abs(offsetMillis.toLong())),
            TimeUnit.MILLISECONDS.toMinutes(abs(offsetMillis.toLong())) % TimeUnit.HOURS.toMinutes(1)
        )

        holder.binding.title.text = timeZone.getDisplayName(Locale.getDefault())

        holder.binding.checkbox.setOnCheckedChangeListener(null)
        holder.binding.checkbox.isChecked = selected.contains(timeZoneId)

        holder.binding.root.setOnClickListener {
            holder.binding.checkbox.toggle()
        }

        holder.binding.checkbox.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
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

    class ViewHolder(val binding: ItemTimeZoneBinding) : RecyclerView.ViewHolder(binding.root)
}
