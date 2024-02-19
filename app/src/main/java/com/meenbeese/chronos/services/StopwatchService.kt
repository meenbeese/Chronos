package com.meenbeese.chronos.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

import androidx.core.app.NotificationCompat

import com.meenbeese.chronos.Chronos
import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.utils.FormatUtils.formatMillis

import java.lang.ref.WeakReference


class StopwatchService : Service() {
    private val binder: IBinder = LocalBinder()
    private var listener: Listener? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var stopTime: Long = 0
    internal var laps: MutableList<Long>? = null
    var lastLapTime: Long = 0
        private set
    var isRunning = false
        private set
    private var notificationText: String? = null
    private var notificationManager: NotificationManager? = null
    private var receiver: NotificationReceiver? = null
    override fun onCreate() {
        super.onCreate()
        receiver = NotificationReceiver(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        laps = ArrayList()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    val currentTime = System.currentTimeMillis() - startTime
                    var text = formatMillis(currentTime)
                    listener?.onTick(currentTime, text)
                    text = text.substring(0, text.length - 3)
                    if (notificationText == null || notificationText != text) {
                        startForeground(NOTIFICATION_ID, getNotification(text))
                        notificationText = text
                    }
                    handler?.removeCallbacks(this)
                    handler?.postDelayed(this, 10)
                } else if (listener != null) {
                    val time = if (startTime == 0L) 0 else stopTime - startTime
                    listener?.onTick(time, formatMillis(time))
                }
            }
        }
        startForeground(NOTIFICATION_ID, getNotification("0s"))
        handler?.postDelayed(runnable!!, 1000)
        val filter = IntentFilter()
        filter.addAction(ACTION_RESET)
        filter.addAction(ACTION_TOGGLE)
        filter.addAction(ACTION_LAP)
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        receiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    val elapsedTime: Long
        get() = stopTime - startTime

    /**
     * Reset the stopwatch, cancelling any notifications and setting everything to zero.
     */
    fun reset() {
        if (isRunning) toggle()
        startTime = 0
        pauseTime = 0
        handler?.post(runnable!!)
        laps?.clear()
        lastLapTime = 0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) stopForeground(true)
        listener?.onReset()
    }

    /**
     * Toggle whether the stopwatch is currently running (pausing it and storing a temporary
     * time if so).
     */
    fun toggle() {
        stopTime = System.currentTimeMillis()
        isRunning = !isRunning
        if (isRunning) {
            if (startTime == 0L) startTime =
                System.currentTimeMillis() else if (pauseTime != 0L) startTime += System.currentTimeMillis() - pauseTime
            handler?.post(runnable!!)
        } else pauseTime = System.currentTimeMillis()
        notificationText = formatMillis(System.currentTimeMillis() - startTime)
        startForeground(NOTIFICATION_ID, getNotification(notificationText!!))
        listener?.onStateChanged(isRunning)
    }

    /**
     * Record the current time as a "lap".
     */
    fun lap() {
        val lapTime = System.currentTimeMillis() - startTime
        val lapDiff = lapTime - lastLapTime
        laps?.add(lapDiff)
        val lastLastLapTime = lastLapTime
        lastLapTime = lapTime
        listener?.onLap(laps!!.size, lapTime, lastLastLapTime, lapDiff)
    }

    /**
     * Get a notification to send to the user for the current time.
     *
     * @param time      A formatted string defining the current time on the stopwatch.
     * @return          A notification to use for this stopwatch.
     */
    private fun getNotification(time: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notificationManager!!.createNotificationChannel(
            NotificationChannel(
                Chronos.NOTIFICATION_CHANNEL_STOPWATCH,
                getString(R.string.title_stopwatch),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        return NotificationCompat.Builder(this, Chronos.NOTIFICATION_CHANNEL_STOPWATCH)
            .setSmallIcon(R.drawable.ic_stopwatch_notification)
            .setContentTitle(getString(R.string.title_stopwatch))
            .setContentText(time)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).putExtra(
                        MainActivity.EXTRA_FRAGMENT,
                        MainActivity.FRAGMENT_STOPWATCH
                    ),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setDeleteIntent(
                PendingIntent.getBroadcast(
                    this, 0, Intent(ACTION_RESET).setPackage(
                        packageName
                    ), PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                if (isRunning) R.drawable.ic_pause_notification else R.drawable.ic_play_notification,
                if (isRunning) "Pause" else "Play",
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_TOGGLE),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                R.drawable.ic_lap_notification,
                "Lap",
                PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(ACTION_LAP),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        listener = null
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        val service: StopwatchService
            get() = this@StopwatchService
    }

    interface Listener {
        fun onStateChanged(isRunning: Boolean)
        fun onReset()
        fun onTick(currentTime: Long, text: String)
        fun onLap(lapNum: Int, lapTime: Long, lastLapTime: Long, lapDiff: Long)
    }

    private class NotificationReceiver(service: StopwatchService) : BroadcastReceiver() {
        private val serviceReference: WeakReference<StopwatchService>

        init {
            serviceReference = WeakReference(service)
        }

        override fun onReceive(context: Context, intent: Intent) {
            val service = serviceReference.get()
            if (intent.action != null && service != null) {
                when (intent.action) {
                    ACTION_RESET -> service.reset()
                    ACTION_TOGGLE -> service.toggle()
                    ACTION_LAP -> service.lap()
                }
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 247
        private const val ACTION_RESET = "james.chronos.StopwatchFragment.ACTION_RESET"
        private const val ACTION_TOGGLE = "james.chronos.StopwatchFragment.ACTION_TOGGLE"
        private const val ACTION_LAP = "james.chronos.StopwatchFragment.ACTION_LAP"
    }
}
