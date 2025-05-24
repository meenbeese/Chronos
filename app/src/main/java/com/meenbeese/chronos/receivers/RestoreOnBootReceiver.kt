package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Chronos

class RestoreOnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chronos = context.applicationContext as Chronos
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (alarm in chronos.alarms) {
            if (alarm.isEnabled) alarm.set(context)
        }
        for (timer in chronos.timers) {
            if (timer.remainingMillis > 0) timer.setAlarm(context, manager)
        }
    }
}
