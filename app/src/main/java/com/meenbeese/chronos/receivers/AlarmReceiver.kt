package com.meenbeese.chronos.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.meenbeese.chronos.activities.AlarmActivity
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.services.AlarmTileService

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        val isPre = intent.getBooleanExtra(EXTRA_PRE_NOTIFICATION, false)
        Log.d("AlarmReceiver", "Alarm received with id: $alarmId (pre=$isPre)")

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
                    sound = it.sound?.let { sound -> SoundData.fromString(sound).getOrNull() },
                    preNotificationMinutes = it.preNotificationMinutes,
                    preNotificationText = it.preNotificationText
                )

                if (isPre) {
                    // Show a light pre-notification and do not launch AlarmActivity.
                    val notificationText = alarm.preNotificationText
                        ?: "Upcoming alarm: ${alarm.name ?: "Alarm"} in ${alarm.preNotificationMinutes} min"
                    val channelId = context.getString(com.meenbeese.chronos.R.string.notification_channel_id)
                    val contentIntent = PendingIntent.getActivity(
                        context,
                        alarmId,
                        Intent(context, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) },
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    val notification = NotificationCompat.Builder(context, channelId)
                        .setContentTitle(context.getString(com.meenbeese.chronos.R.string.app_name))
                        .setContentText(notificationText)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent)
                        .build()

                    val preNotificationId = 1_000_000 + alarmId
                    val nm = NotificationManagerCompat.from(context)
                    nm.notify(preNotificationId, notification)
                    return@launch
                }

                // Main alarm behavior: reschedule if repeating or disable and save, then start AlarmActivity
                if (alarm.getNext() != null) {
                    alarm.set(context)
                } else {
                    alarm.isEnabled = false
                    alarm.saveToDatabase(context)
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
        const val EXTRA_PRE_NOTIFICATION = "meenbeese.chronos.EXTRA_PRE_NOTIFICATION"
    }
}
