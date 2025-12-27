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

import com.meenbeese.chronos.R
import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.utils.FormatUtils.formatUnit

import org.koin.android.ext.android.inject

import java.util.Calendar
import java.util.concurrent.TimeUnit

class SleepReminderService : Service() {
    private val repo: TimerAlarmRepository by inject()

    private var powerManager: PowerManager? = null
    private var receiver: ScreenReceiver? = null

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        receiver = ScreenReceiver(this)
        refreshState()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(receiver, filter)
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
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
        val pm = powerManager ?: run {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        if (!pm.isInteractive) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val nextAlarm = getSleepyAlarm(applicationContext, repo)
        val nextInstance = nextAlarm?.getNext()

        if (nextInstance == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val minutesUntil = TimeUnit.MILLISECONDS.toMinutes(
            nextInstance.timeInMillis - System.currentTimeMillis()
        ).toInt()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                "sleepReminder",
                getString(R.string.title_sleep_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val notification = NotificationCompat.Builder(this, "sleepReminder")
            .setContentTitle(getString(R.string.title_sleep_reminder))
            .setContentText(getString(R.string.msg_sleep_reminder, formatUnit(this, minutesUntil)))
            .setSmallIcon(R.drawable.ic_notification_sleep)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(540, notification)
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
        fun getSleepyAlarm(context: Context, repo: TimerAlarmRepository): AlarmData? {
            if (Preferences.SLEEP_REMINDER.get(context)) {
                val nextAlarm = getNextWakeAlarm(repo)
                if (nextAlarm != null) {
                    val nextTrigger = nextAlarm.getNext()!!
                    nextTrigger[Calendar.MINUTE] -= TimeUnit.MILLISECONDS.toMinutes(
                        Preferences.SLEEP_REMINDER_TIME.get(context)
                    ).toInt()

                    if (Calendar.getInstance().after(nextTrigger)) return nextAlarm
                }
            }
            return null
        }

        private fun getNextWakeAlarm(repo: TimerAlarmRepository): AlarmData? {
            val alarms = repo.alarms
            val nextNoon = Calendar.getInstance()
            nextNoon[Calendar.HOUR_OF_DAY] = 12
            if (nextNoon.before(Calendar.getInstance())) nextNoon[Calendar.DAY_OF_YEAR] =
                nextNoon[Calendar.DAY_OF_YEAR] + 1 else return null
            val nextDay = Calendar.getInstance()
            nextDay[Calendar.HOUR_OF_DAY] = 0
            while (nextDay.before(Calendar.getInstance())) nextDay[Calendar.DAY_OF_YEAR] =
                nextDay[Calendar.DAY_OF_YEAR] + 1
            var nextAlarm: AlarmData? = null
            for (alarm in alarms) {
                val next = alarm.getNext()
                if (alarm.isEnabled &&
                    next!!.before(nextNoon) &&
                    next.after(nextDay) &&
                    (nextAlarm == null || nextAlarm.getNext()!!.after(next))
                ) {
                    nextAlarm = alarm
                }
            }
            return nextAlarm
        }

        fun refreshSleepTime(context: Context, repo: TimerAlarmRepository) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE)
                != PackageManager.PERMISSION_GRANTED
            ) return

            val sleepyAlarm = getSleepyAlarm(context, repo)
            if (sleepyAlarm != null) {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, SleepReminderService::class.java)
                )
            }
        }
    }
}
