package com.meenbeese.chronos.db

import android.app.Application
import android.content.Intent
import android.os.Build

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.toNullable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.Calendar

class TimerAlarmRepository(
    private val application: Application,
    private val alarmDao: AlarmDao
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _alarms = mutableListOf<AlarmData>()
    val alarms: List<AlarmData> get() = _alarms

    private val _timers = mutableListOf<TimerData>()
    val timers: List<TimerData> get() = _timers

    init {
        observeAlarms()
        loadTimers()
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
                            time = Calendar.getInstance().apply { timeInMillis = entity.timeInMillis },
                            isEnabled = entity.isEnabled,
                            days = entity.days.toMutableList(),
                            isVibrate = entity.isVibrate,
                            sound = entity.sound?.let { SoundData.fromString(it).toNullable() }
                        )
                    }
                )
            }
        })
    }

    private fun loadTimers() {
        val timerLength = Preferences.TIMER_LENGTH.get(application)
        for (id in 0 until timerLength) {
            val timer = TimerData(id, application)
            if (timer.isSet) _timers.add(timer)
        }

        if (timerLength > 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                application.startForegroundService(Intent(application, TimerService::class.java))
            } else {
                application.startService(Intent(application, TimerService::class.java))
            }
        }
    }

    fun newTimer(): TimerData {
        val timer = TimerData(_timers.size, application)
        _timers.add(timer)
        coroutineScope.launch {
            Preferences.TIMER_LENGTH.set(application, _timers.size)
        }
        return timer
    }

    fun removeTimer(timer: TimerData) {
        timer.onRemoved(application)
        val index = _timers.indexOf(timer)
        if (index != -1) {
            _timers.removeAt(index)
            for (i in index until _timers.size) {
                _timers[i].onIdChanged(i, application)
            }
            coroutineScope.launch {
                Preferences.TIMER_LENGTH.set(application, _timers.size)
            }
        }
    }
}
