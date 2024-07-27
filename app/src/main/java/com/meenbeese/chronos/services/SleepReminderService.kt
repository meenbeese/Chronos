package com.meenbeese.chronos.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.utils.FormatUtils.formatUnit

import java.util.Calendar
import java.util.concurrent.TimeUnit


class SleepReminderService : Service() {
    private var chronos: Chronos? = null
    private var powerManager: PowerManager? = null
    private var receiver: ScreenReceiver? = null

    override fun onCreate() {
        super.onCreate()
        chronos = applicationContext as Chronos
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        receiver = ScreenReceiver(this)
        refreshState()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshState()
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Refresh the state of the sleepy stuff. This will either show a notification if a notification
     * should be shown, or stop the service if it shouldn't.
     */
    fun refreshState() {
        if (powerManager!!.isInteractive) {
            val nextAlarm = getSleepyAlarm(chronos)
            if (nextAlarm != null) {
                val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    manager.createNotificationChannel(
                        NotificationChannel(
                            "sleepReminder",
                            getString(R.string.title_sleep_reminder),
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                    )
                    NotificationCompat.Builder(this, "sleepReminder")
                } else NotificationCompat.Builder(this)
                startForeground(
                    540, builder.setContentTitle(getString(R.string.title_sleep_reminder))
                        .setContentText(
                            String.format(
                                getString(R.string.msg_sleep_reminder),
                                formatUnit(
                                    this,
                                    TimeUnit.MILLISECONDS.toMinutes(nextAlarm.next!!.timeInMillis - System.currentTimeMillis())
                                        .toInt()
                                )
                            )
                        )
                        .setSmallIcon(R.drawable.ic_notification_sleep)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .build()
                )
                return
            }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private class ScreenReceiver(private val service: SleepReminderService) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            service.refreshState()
        }
    }

    companion object {
        /**
         * Get a sleepy alarm. Well, get the next alarm that should trigger a sleep alert.
         *
         * @param chronos       The active Application instance.
         * @return              The next [AlarmData](../data/AlarmData) that should trigger a
         * sleep alert, or null if there isn't one.
         */
        fun getSleepyAlarm(chronos: Chronos?): AlarmData? {
            if (PreferenceData.SLEEP_REMINDER.getValue(chronos)) {
                val nextAlarm = getNextWakeAlarm(chronos)
                if (nextAlarm != null) {
                    val nextTrigger = nextAlarm.next!!
                    nextTrigger[Calendar.MINUTE] =
                        nextTrigger[Calendar.MINUTE] - TimeUnit.MILLISECONDS.toMinutes(
                            PreferenceData.SLEEP_REMINDER_TIME.getValue(chronos)
                        ).toInt()
                    if (Calendar.getInstance().after(nextTrigger)) return nextAlarm
                }
            }
            return null
        }

        /**
         * Get the next scheduled [AlarmData](../data/AlarmData) that will ring.
         *
         * @param chronos       The active Application instance.
         * @return              The next AlarmData that will wake the user up.
         */
        private fun getNextWakeAlarm(chronos: Chronos?): AlarmData? {
            val nextNoon = Calendar.getInstance()
            nextNoon[Calendar.HOUR_OF_DAY] = 12
            if (nextNoon.before(Calendar.getInstance())) nextNoon[Calendar.DAY_OF_YEAR] =
                nextNoon[Calendar.DAY_OF_YEAR] + 1 else return null
            val nextDay = Calendar.getInstance()
            nextDay[Calendar.HOUR_OF_DAY] = 0
            while (nextDay.before(Calendar.getInstance())) nextDay[Calendar.DAY_OF_YEAR] =
                nextDay[Calendar.DAY_OF_YEAR] + 1
            val alarms = chronos!!.alarms
            var nextAlarm: AlarmData? = null
            for (alarm in alarms) {
                val next = alarm.next
                if (alarm.isEnabled && next!!.before(nextNoon) && next.after(nextDay) && (nextAlarm == null || nextAlarm.next!!
                        .after(next))
                ) nextAlarm = alarm
            }
            return nextAlarm
        }

        /**
         * To be called whenever an alarm is changed, might change, or when time might have
         * unexpectedly leaped forwards. This will start the service if there is a
         * [sleepy alarm](#getsleepyalarm) present.
         *
         * @param context       An active context instance.
         */
        @JvmStatic
        fun refreshSleepTime(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.FOREGROUND_SERVICE
                ) != PackageManager.PERMISSION_GRANTED
            ) return

            val chronos = (context as? Chronos) ?: (context.applicationContext as? Chronos)
            chronos?.let {
                val sleepyAlarm = getSleepyAlarm(it)
                if (sleepyAlarm != null) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(it, SleepReminderService::class.java)
                    )
                }
            }
        }
    }
}
