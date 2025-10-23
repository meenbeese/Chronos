package com.meenbeese.chronos.db

import android.app.Application
import android.content.Intent
import android.os.Build

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerAlarmRepository(
    private val application: Application,
    private val alarmDao: AlarmDao
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _alarms = mutableListOf<AlarmData>()
    val alarms: List<AlarmData> get() = _alarms

    init {
        // Only observe alarms — timers removed in this fork
        observeAlarms()
    }

    private fun observeAlarms() {
        val liveAlarms: LiveData<List<AlarmEntity>> = alarmDao.getAllAlarms()
        liveAlarms.observeForever(object : Observer<List<AlarmEntity>> {
            override fun onChanged(value: List<AlarmEntity>) {
                _alarms.clear()
                _alarms.addAll(
                    value.map { entity ->
                        AlarmData(
                            id = entity.id,
                            name = entity.name,
                            time = java.util.Calendar.getInstance().apply { timeInMillis = entity.timeInMillis },
                            isEnabled = entity.isEnabled,
                            days = entity.days.toMutableList(),
                            isVibrate = entity.isVibrate,
                            sound = entity.sound?.let { SoundData.fromString(it).getOrNull() },
                            preNotificationMinutes = entity.preNotificationMinutes,
                            preNotificationText = entity.preNotificationText
                        )
                    }
                )
            }
        })
    }

    fun newAlarm(): AlarmData {
        val alarm = AlarmData(0)
        _alarms.add(alarm)
        coroutineScope.launch {
            Preferences.TIMER_LENGTH.set(application, _alarms.size) // This line can be cleaned up if TIMER_LENGTH preference is removed.
        }
        return alarm
    }

    fun removeAlarm(alarm: AlarmData) {
        alarm.deleteFromDatabase(application)
        val index = _alarms.indexOf(alarm)
        if (index != -1) {
            _alarms.removeAt(index)
        }
    }

    // (Other alarm-related repository methods remain available here; timer-specific APIs were removed)
}
