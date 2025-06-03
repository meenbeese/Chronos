package com.meenbeese.chronos.services

import android.annotation.SuppressLint
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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

import androidx.core.app.NotificationCompat

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.utils.FormatUtils.formatMillis

class StopwatchService : Service() {
    private val binder: IBinder = LocalBinder()

    private var listener: Listener? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null

    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var stopTime: Long = 0

    internal var laps: MutableList<Long>? = null

    val elapsedTime: Long
        get() = stopTime - startTime

    var lastLapTime: Long = 0
        private set

    var isRunning = false
        private set

    private var notificationText: String? = null
    private var notificationManager: NotificationManager? = null
    private var receiver: NotificationReceiver? = null

    @SuppressLint("NewApi")
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

        val filter = IntentFilter().apply {
            addAction(ACTION_RESET)
            addAction(ACTION_TOGGLE)
            addAction(ACTION_LAP)
        }

        registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
    }

    override fun onDestroy() {
        receiver?.let { unregisterReceiver(it) }
        super.onDestroy()
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    /**
     * Reset the stopwatch, cancelling any notifications and setting everything to zero.
     */
    fun reset() {
        if (isRunning) {
            toggle()
        }

        startTime = 0
        pauseTime = 0
        lastLapTime = 0

        handler?.post(runnable!!)
        laps?.clear()

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
            startTime = when {
                startTime == 0L -> System.currentTimeMillis()
                pauseTime != 0L -> startTime + System.currentTimeMillis() - pauseTime
                else -> startTime
            }
            handler?.post(runnable!!)
        } else {
            pauseTime = System.currentTimeMillis()
        }

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
        notificationManager!!.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_STOPWATCH,
                getString(R.string.title_stopwatch),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val actionIcon = if (isRunning) R.drawable.ic_pause_notification else R.drawable.ic_play_notification
        val actionText = if (isRunning) "Pause" else "Play"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_STOPWATCH)
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
                    this,
                    REQUEST_CODE_RESET,
                    Intent(ACTION_RESET).setPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(actionIcon, actionText,
                PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE_TOGGLE,
                    Intent(ACTION_TOGGLE).setPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .addAction(
                R.drawable.ic_lap_notification, "Lap",
                PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE_LAP,
                    Intent(ACTION_LAP).setPackage(packageName),
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

    private class NotificationReceiver(private val service: StopwatchService) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                Log.d("StopwatchService", "Received action: ${intent.action}")
                when (intent.action) {
                    ACTION_RESET -> service.reset()
                    ACTION_TOGGLE -> service.toggle()
                    ACTION_LAP -> service.lap()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_TOGGLE = 100
        private const val REQUEST_CODE_RESET = 101
        private const val REQUEST_CODE_LAP = 102
        private const val NOTIFICATION_ID = 247
        private const val NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch"
        private const val ACTION_RESET = "meenbeese.chronos.StopwatchFragment.ACTION_RESET"
        private const val ACTION_TOGGLE = "meenbeese.chronos.StopwatchFragment.ACTION_TOGGLE"
        private const val ACTION_LAP = "meenbeese.chronos.StopwatchFragment.ACTION_LAP"
    }
}
