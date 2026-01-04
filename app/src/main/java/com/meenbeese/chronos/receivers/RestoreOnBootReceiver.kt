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

        repo.alarms.value
            ?.filter { it.isEnabled }
            ?.forEach { alarm ->
                alarm.set(context)
            }

        repo.timers.value
            ?.filter { it.remainingMillis > 0 }
            ?.forEach { timer ->
                timer.setAlarm(context, manager)
            }
    }
}
