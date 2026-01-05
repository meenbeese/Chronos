package com.meenbeese.chronos.db

import android.app.Application
import android.content.Intent
import android.os.Build

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.data.toData
import com.meenbeese.chronos.services.TimerService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerAlarmRepository(
    private val application: Application,
    private val alarmRepository: AlarmRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _alarms = MutableLiveData<List<AlarmData>>(emptyList())
    val alarms: LiveData<List<AlarmData>> get() = _alarms

    private val _timers = MutableLiveData<List<TimerData>>(emptyList())
    val timers: LiveData<List<TimerData>> get() = _timers

    init {
        observeAlarms()
        loadTimers()
    }

    private fun observeAlarms() {
        alarmRepository.getAll().observeForever { entities ->
            _alarms.postValue(
                entities.map { it.toData() }
            )
        }
    }

    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmRepository.insert(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmRepository.update(alarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmRepository.delete(alarm)
    }

    private fun loadTimers() {
        val timerLength = Preferences.TIMER_LENGTH.get(application)
        val loaded = mutableListOf<TimerData>()

        for (id in 0 until timerLength) {
            val timer = TimerData(id, application)
            if (timer.isSet) loaded.add(timer)
        }

        _timers.postValue(loaded)

        if (loaded.isNotEmpty()) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                application.startForegroundService(Intent(application, TimerService::class.java))
            } else {
                application.startService(Intent(application, TimerService::class.java))
            }
        }
    }

    fun newTimer(): TimerData {
        val timer = TimerData(_timers.value!!.size, application)
        val updated = _timers.value!!.toMutableList().apply { add(timer) }

        _timers.postValue(updated)
        coroutineScope.launch {
            Preferences.TIMER_LENGTH.set(application, updated.size)
        }

        return timer
    }

    fun removeTimer(timer: TimerData) {
        timer.onRemoved(application)

        val updated = _timers.value!!.toMutableList()
        val index = updated.indexOf(timer)
        if (index == -1) return

        updated.removeAt(index)
        updated.forEachIndexed { i, t ->
            t.onIdChanged(i, application)
        }

        _timers.postValue(updated)

        coroutineScope.launch {
            Preferences.TIMER_LENGTH.set(application, updated.size)
        }
    }
}
