package com.meenbeese.chronos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.activities.AlarmActivity
import com.meenbeese.chronos.db.TimerAlarmRepository

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimerReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: TimerAlarmRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getIntExtra(EXTRA_TIMER_ID, 0)
        val timer = repo.timers.value
            ?.getOrNull(timerId)
            ?: return

        repo.removeTimer(timer)

        val ringer = Intent(context, AlarmActivity::class.java)
        ringer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ringer.putExtra(AlarmActivity.EXTRA_TIMER, timer)
        context.startActivity(ringer)
    }

    companion object {
        const val EXTRA_TIMER_ID = "meenbeese.chronos.EXTRA_TIMER_ID"
    }
}
