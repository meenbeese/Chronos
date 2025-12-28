package com.meenbeese.chronos.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log

import com.meenbeese.chronos.activities.AlarmActivity
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.services.AlarmTileService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import java.util.Calendar

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val alarmRepository: AlarmRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        Log.d("AlarmReceiver", "Alarm received with id: $alarmId")

        if (alarmId == -1) {
            Log.w("AlarmReceiver", "No alarm ID found in intent!")
            return
        }
        val db = AlarmDatabase.getDatabase(context).alarmDao()

        CoroutineScope(Dispatchers.IO).launch {
            val alarmEntity = db.getAlarmById(alarmId)
            alarmEntity?.let {
                val alarm = AlarmData(
                    id = it.id,
                    name = it.name,
                    time = Calendar.getInstance().apply { timeInMillis = it.timeInMillis },
                    isEnabled = it.isEnabled,
                    days = it.days.toMutableList(),
                    isVibrate = it.isVibrate,
                    sound = it.sound?.let { sound -> SoundData.fromString(sound).getOrNull() }
                )

                if (alarm.getNext() != null) {
                    alarm.set(context)
                } else {
                    alarm.isEnabled = false
                    alarmRepository.saveAlarm(
                        alarm.id,
                        alarm.name,
                        alarm.time.timeInMillis,
                        alarm.isEnabled,
                        alarm.days,
                        alarm.isVibrate,
                        alarm.sound?.toString()
                    )
                }

                TileService.requestListeningState(
                    context,
                    ComponentName(context, AlarmTileService::class.java)
                )

                withContext(Dispatchers.Main) {
                    val ringer = Intent(context, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(AlarmActivity.EXTRA_ALARM, alarm)
                    }
                    context.startActivity(ringer)
                }
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "meenbeese.chronos.EXTRA_ALARM_ID"
    }
}
