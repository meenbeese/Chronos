package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Alarmio
import com.meenbeese.chronos.activities.AlarmActivity


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmio = context.applicationContext as Alarmio
        val alarm = alarmio.alarms[intent.getIntExtra(EXTRA_ALARM_ID, 0)]
        if (alarm.isRepeat) alarm[context] = manager else alarm.setEnabled(alarmio, manager, false)
        alarmio.onAlarmsChanged()
        val ringer = Intent(context, AlarmActivity::class.java)
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ringer.putExtra(AlarmActivity.EXTRA_ALARM, alarm)
        context.startActivity(ringer)
    }

    companion object {
        const val EXTRA_ALARM_ID = "james.alarmio.EXTRA_ALARM_ID"
    }
}
