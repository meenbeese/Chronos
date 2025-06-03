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
import com.meenbeese.chronos.utils.Theme
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
        activityTheme = Theme.fromInt(PreferenceData.THEME.getValue(this))

        val liveAlarms = alarmDao.getAllAlarms()
        liveAlarms.observeForever { alarmEntities ->
            alarms.clear()
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
    @OptIn(DelicateCoroutinesApi::class)
    fun newTimer(): TimerData {
        val timer = TimerData(timers.size)
        timers.add(timer)
        GlobalScope.launch {
            PreferenceData.TIMER_LENGTH.setValue(this@Chronos, timers.size)
        }
        return timer
    }

    /**
     * Remove a timer and all of its preferences.
     *
     * @param timer     The timer to be removed.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun removeTimer(timer: TimerData) {
        timer.onRemoved(this)
        val index = timers.indexOf(timer)
        timers.removeAt(index)
        for (i in index until timers.size) {
            timers[i].onIdChanged(i, this)
        }
        GlobalScope.launch {
            PreferenceData.TIMER_LENGTH.setValue(this@Chronos, timers.size)
        }
        for (listener in listeners!!) {
            listener.onTimersChanged()
        }
    }

    val isNight: Boolean
        /**
         * Determine if the theme should be a night theme.
         *
         * @return          True if the current theme is a night theme.
         */
        get() {
            val currentHour = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
            }.get(Calendar.HOUR_OF_DAY)

            return currentHour < 6 || currentHour >= 18
        }
    var activityTheme: Theme = Theme.AUTO
        private set

    fun isDarkTheme(): Boolean {
        return activityTheme == Theme.NIGHT ||
               activityTheme == Theme.AMOLED ||
               (activityTheme == Theme.AUTO && isNight)
    }

    suspend fun applyAndSaveTheme(context: Context, theme: Theme) {
        activityTheme = theme
        PreferenceData.THEME.setValue(context, theme.value)
        when (theme) {
            Theme.AUTO   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            Theme.DAY    -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            Theme.NIGHT,
            Theme.AMOLED -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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

    interface ChronosListener {
        fun onAlarmsChanged()
        fun onTimersChanged()
    }

    interface ActivityListener {
        fun fetchFragmentManager(): FragmentManager?
        fun getActivity(): AppCompatActivity?
    }
}
