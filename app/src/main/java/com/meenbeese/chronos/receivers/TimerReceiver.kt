package com.meenbeese.chronos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Alarmio
import com.meenbeese.chronos.activities.AlarmActivity


class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmio = context.applicationContext as Alarmio
        val timer = alarmio.timers[intent.getIntExtra(EXTRA_TIMER_ID, 0)]
        alarmio.removeTimer(timer)
        val ringer = Intent(context, AlarmActivity::class.java)
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ringer.putExtra(AlarmActivity.EXTRA_TIMER, timer)
        context.startActivity(ringer)
    }

    companion object {
        const val EXTRA_TIMER_ID = "james.alarmio.EXTRA_TIMER_ID"
    }
}
