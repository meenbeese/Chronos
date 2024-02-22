package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.activities.AlarmActivity


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val chronos = context.applicationContext as Chronos
        val alarm = chronos.alarms[intent.getIntExtra(EXTRA_ALARM_ID, 0)]
        if (alarm.isRepeat) alarm[context] = manager else alarm.setEnabled(chronos, manager, false)
        chronos.onAlarmsChanged()
        val ringer = Intent(context, AlarmActivity::class.java)
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ringer.putExtra(AlarmActivity.EXTRA_ALARM, alarm)
        context.startActivity(ringer)
    }

    companion object {
        const val EXTRA_ALARM_ID = "meenbeese.chronos.EXTRA_ALARM_ID"
    }
}
