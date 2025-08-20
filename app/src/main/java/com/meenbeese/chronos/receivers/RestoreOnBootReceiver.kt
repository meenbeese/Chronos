package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.db.TimerAlarmRepository

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RestoreOnBootReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: TimerAlarmRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (alarm in repo.alarms) {
            if (alarm.isEnabled) alarm.set(context)
        }

        for (timer in repo.timers) {
            if (timer.remainingMillis > 0) timer.setAlarm(context, manager)
        }
    }
}
