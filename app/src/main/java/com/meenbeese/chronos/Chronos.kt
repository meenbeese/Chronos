package com.meenbeese.chronos

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentManager

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.PreferenceData
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.db.AlarmDao
import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.services.SleepReminderService.Companion.refreshSleepTime
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.utils.CoreHelper
import com.meenbeese.chronos.utils.toNullable

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.Calendar

class Chronos : Application() {
    lateinit var alarms: ArrayList<AlarmData>
    lateinit var timers: ArrayList<TimerData>
    private var listeners: MutableList<ChronosListener>? = null
    private var listener: ActivityListener? = null

    lateinit var database: AlarmDatabase
    lateinit var alarmDao: AlarmDao
    lateinit var repository: AlarmRepository

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        CoreHelper.contextGetter = { this }

        database = AlarmDatabase.getDatabase(this)
        alarmDao = database.alarmDao()
        repository = AlarmRepository(alarmDao)

        listeners = ArrayList()
        alarms = ArrayList()
        timers = ArrayList()
        activityTheme = PreferenceData.THEME.getValue<Int>(this).mapTheme()

        GlobalScope.launch {
            val alarmEntities = alarmDao.getAllAlarms()
            alarms.addAll(alarmEntities.map { entity ->
                AlarmData(
                    id = entity.id,
                    name = entity.name,
                    time = Calendar.getInstance().apply { timeInMillis = entity.timeInMillis },
                    isEnabled = entity.isEnabled,
                    days = entity.days,
                    isVibrate = entity.isVibrate,
                    sound = entity.sound?.let { SoundData.fromString(it).toNullable() }
                )
            })
        }

        val timerLength = PreferenceData.TIMER_LENGTH.getValue<Int>(this)
        for (id in 0 until timerLength) {
            val timer = TimerData(id, this)
            if (timer.isSet) timers.add(timer)
        }

        if (timerLength > 0) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, TimerService::class.java))
            } else {
                startService(Intent(this, TimerService::class.java))
            }
        }
        refreshSleepTime(this)
    }

    /**
     * Create a new timer, assigning it an unused preference id.
     *
     * @return          The newly instantiated [TimerData](./data/TimerData).
     */
    fun newTimer(): TimerData {
        val timer = TimerData(timers.size)
        timers.add(timer)
        onTimerCountChanged()
        return timer
    }

    /**
     * Remove a timer and all of its preferences.
     *
     * @param timer     The timer to be removed.
     */
    fun removeTimer(timer: TimerData) {
        timer.onRemoved(this)
        val index = timers.indexOf(timer)
        timers.removeAt(index)
        for (i in index until timers.size) {
            timers[i].onIdChanged(i, this)
        }
        onTimerCountChanged()
        onTimersChanged()
    }

    /**
     * Update the preferences to show that the timer count has been changed.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun onTimerCountChanged() {
        GlobalScope.launch {
            PreferenceData.TIMER_LENGTH.setValue(this@Chronos, timers.size)
        }
    }

    /**
     * Notify the application of changes to the current timers.
     */
    private fun onTimersChanged() {
        for (listener in listeners!!) {
            listener.onTimersChanged()
        }
    }

    /**
     * Starts the timer service after a timer has been set.
     */
    fun onTimerStarted() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, TimerService::class.java))
        } else {
            startService(Intent(this, TimerService::class.java))
        }
    }

    val isNight: Boolean
        /**
         * Determine if the theme should be a night theme.
         *
         * @return          True if the current theme is a night theme.
         */
        get() {
            val time = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            return time < dayStart || time > dayEnd
        }
    var activityTheme: Int = THEME_AUTO
        private set
    val dayStart: Int
        /**
         * @return the hour of the start of the day (24h), as specified by the user
         */
        get() = PreferenceData.DAY_START.getValue(this)
    val dayEnd: Int
        /**
         * @return the hour of the end of the day (24h), as specified by the user
         */
        get() = PreferenceData.DAY_END.getValue(this)

    fun isDarkTheme(): Boolean {
        return activityTheme == THEME_NIGHT || activityTheme == THEME_AMOLED || (activityTheme == THEME_AUTO && isNight)
    }

    suspend fun applyAndSaveTheme(context: Context, theme: Int) {
        activityTheme = theme
        PreferenceData.THEME.setValue(context, theme)
        when (theme) {
            THEME_AUTO   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            THEME_DAY    -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_NIGHT  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_AMOLED -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun addListener(listener: ChronosListener) {
        listeners?.add(listener)
    }

    fun removeListener(listener: ChronosListener) {
        listeners?.remove(listener)
    }

    fun setListener(listener: ActivityListener?) {
        this.listener = listener
    }

    val fragmentManager: FragmentManager?
        get() = if (listener != null) listener!!.fetchFragmentManager() else null

    /**
     * Recreate the current activity to apply the new theme.
     */
    fun recreate() {
        listener?.let {
            it.getActivity()?.recreate()
        }
    }

    interface ChronosListener {
        fun onAlarmsChanged()
        fun onTimersChanged()
    }

    interface ActivityListener {
        fun fetchFragmentManager(): FragmentManager?
        fun getActivity(): AppCompatActivity?
    }

    companion object {
        const val THEME_AUTO = 0
        const val THEME_DAY = 1
        const val THEME_NIGHT = 2
        const val THEME_AMOLED = 3
        const val NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch"
        const val NOTIFICATION_CHANNEL_TIMERS = "timers"

        fun Int.mapTheme(): Int {
            return when (this) {
                0 -> THEME_AUTO
                1 -> THEME_DAY
                2 -> THEME_NIGHT
                3 -> THEME_AMOLED
                else -> -1
            }
        }
    }
}
