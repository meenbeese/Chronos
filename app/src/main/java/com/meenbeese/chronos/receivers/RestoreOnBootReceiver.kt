package com.meenbeese.chronos.receivers

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.data.toData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RestoreOnBootReceiver : BroadcastReceiver(), KoinComponent {

    private val repo: TimerAlarmRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingResult = goAsync()
        val isLockedBoot = intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AlarmDatabase.getDatabase(context)
                val enabledAlarms = db.alarmDao()
                    .getAllAlarmsDirect()
                    .filter { it.isEnabled }
                    .map { it.toData() }

                enabledAlarms.forEach { alarm ->
                    alarm.set(context)
                }

                if (!isLockedBoot) {
                    repo.timers.value
                        ?.filter { it.remainingMillis > 0 }
                        ?.forEach { timer ->
                            timer.setAlarm(context, manager)
                        }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
