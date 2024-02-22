package com.meenbeese.chronos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.activities.AlarmActivity


class TimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chronos = context.applicationContext as Chronos
        val timer = chronos.timers[intent.getIntExtra(EXTRA_TIMER_ID, 0)]
        chronos.removeTimer(timer)
        val ringer = Intent(context, AlarmActivity::class.java)
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ringer.putExtra(AlarmActivity.EXTRA_TIMER, timer)
        context.startActivity(ringer)
    }

    companion object {
        const val EXTRA_TIMER_ID = "meenbeese.chronos.EXTRA_TIMER_ID"
    }
}
