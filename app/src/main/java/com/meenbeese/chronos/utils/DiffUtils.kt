package com.meenbeese.chronos.utils

import androidx.recyclerview.widget.DiffUtil
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.TimerData

class AlarmsDiffCallback(
    private val oldTimers: List<TimerData>,
    private val newTimers: List<TimerData>,
    private val oldAlarms: List<AlarmData>,
    private val newAlarms: List<AlarmData>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldTimers.size + oldAlarms.size
    override fun getNewListSize() = newTimers.size + newAlarms.size

    override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
        val oldItem = getItem(oldTimers, oldAlarms, oldPos)
        val newItem = getItem(newTimers, newAlarms, newPos)
        return when {
            oldItem is TimerData && newItem is TimerData -> oldItem.hashCode() == newItem.hashCode()
            oldItem is AlarmData && newItem is AlarmData -> oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
        val oldItem = getItem(oldTimers, oldAlarms, oldPos)
        val newItem = getItem(newTimers, newAlarms, newPos)
        return oldItem == newItem
    }

    private fun getItem(timers: List<TimerData>, alarms: List<AlarmData>, position: Int): Any? {
        return if (position < timers.size) {
            timers[position]
        } else {
            alarms.getOrNull(position - timers.size)
        }
    }
}
