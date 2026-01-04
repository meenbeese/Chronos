package com.meenbeese.chronos.viewmodel

import android.app.AlarmManager
import android.content.Context

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.meenbeese.chronos.BuildConfig
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.data.toEntity
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.services.TimerService

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.Calendar

class HomeViewModel(
    private val repo: TimerAlarmRepository
) : ViewModel() {

    val alarms: LiveData<List<AlarmData>> = repo.alarms
    val timers: LiveData<List<TimerData>> = repo.timers

    fun updateAlarm(context: Context, alarm: AlarmData) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateAlarm(alarm.toEntity())
            alarm.cancel(context)
            if (alarm.isEnabled) {
                alarm.set(context)
            }
        }
    }

    fun deleteAlarm(context: Context, alarm: AlarmData) {
        viewModelScope.launch(Dispatchers.IO) {
            alarm.cancel(context)
            repo.deleteAlarm(alarm.toEntity())
        }
    }

    fun scheduleAlarm(
        context: Context,
        hour: Int,
        minute: Int,
        onAlarmScheduled: (Long) -> Unit
    ) {
        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (BuildConfig.DEBUG) add(Calendar.MINUTE, 1)
        }.timeInMillis

        val alarm = AlarmData(
            id = 0,
            name = null,
            time = Calendar.getInstance().apply { timeInMillis = triggerTime },
            isEnabled = true,
            days = MutableList(7) { false },
            isVibrate = true,
            sound = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.insertAlarm(alarm.toEntity())
            alarm.id = id.toInt()
            alarm.set(context)
        }

        onAlarmScheduled(triggerTime)
    }

    fun scheduleTimer(
        context: Context,
        hours: Int,
        minutes: Int,
        seconds: Int,
        ringtone: SoundData?,
        vibrate: Boolean,
        onTimerStarted: (TimerData) -> Unit,
        onInvalidDuration: () -> Unit
    ) {
        val totalMillis = ((hours * 3600) + (minutes * 60) + seconds) * 1000L
        if (totalMillis <= 0) {
            onInvalidDuration()
            return
        }

        val timer = repo.newTimer()
        timer.setDuration(totalMillis, context)
        timer.setVibrate(context, vibrate)
        timer.setSound(context, ringtone)
        timer[context] = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        TimerService.startService(context)
        onTimerStarted(timer)
    }
}
