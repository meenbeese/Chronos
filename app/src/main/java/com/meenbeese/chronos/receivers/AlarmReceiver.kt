package com.meenbeese.chronos.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.meenbeese.chronos.activities.AlarmActivity
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.utils.toNullable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
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
                    days = it.days,
                    isVibrate = it.isVibrate,
                    sound = it.sound?.let { SoundData.fromString(it).toNullable() }
                )

                if (alarm.getNext() != null) {
                    alarm.set(context)
                } else {
                    alarm.isEnabled = false
                    alarm.saveToDatabase(context)
                }

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
