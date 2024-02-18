package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Alarmio


class RestoreOnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmio = context.applicationContext as Alarmio
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (alarm in alarmio.alarms) {
            if (alarm.isEnabled) alarm[context] = manager
        }
        for (timer in alarmio.timers) {
            if (timer.remainingMillis > 0) timer.setAlarm(context, manager)
        }
    }
}
