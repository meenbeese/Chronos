package com.meenbeese.chronos

import android.app.Application
import android.content.Intent
import android.os.Build

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

import com.meenbeese.chronos.data.AlarmData
import com.meenbeese.chronos.data.Preferences
import com.meenbeese.chronos.data.SoundData
import com.meenbeese.chronos.data.TimerData
import com.meenbeese.chronos.db.AlarmDao
import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.di.appModule
import com.meenbeese.chronos.services.SleepReminderService.Companion.refreshSleepTime
import com.meenbeese.chronos.services.TimerService
import com.meenbeese.chronos.ui.theme.ThemeMode
import com.meenbeese.chronos.utils.toNullable

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

import java.util.Calendar

class Chronos : Application() {
    lateinit var alarms: ArrayList<AlarmData>
    lateinit var timers: ArrayList<TimerData>
    private var listeners: MutableList<ChronosListener>? = null
    private var listener: ActivityListener? = null

    lateinit var database: AlarmDatabase
    lateinit var alarmDao: AlarmDao
    lateinit var repository: AlarmRepository

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Chronos)
            modules(appModule)
        }

        database = AlarmDatabase.getDatabase(this)
        alarmDao = database.alarmDao()
        repository = AlarmRepository(alarmDao)

        listeners = ArrayList()
        alarms = ArrayList()
        timers = ArrayList()

        val liveAlarms = alarmDao.getAllAlarms()
        liveAlarms.observeForever { alarmEntities ->
            alarms.clear()
            alarms.addAll(alarmEntities.map { entity ->
                AlarmData(
                    id = entity.id,
                    name = entity.name,
                    time = Calendar.getInstance().apply { timeInMillis = entity.timeInMillis },
                    isEnabled = entity.isEnabled,
                    days = entity.days.toMutableList(),
                    isVibrate = entity.isVibrate,
                    sound = entity.sound?.let { SoundData.fromString(it).toNullable() }
                )
            })
        }

        val timerLength = Preferences.TIMER_LENGTH.get(this)
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
            Preferences.TIMER_LENGTH.set(this@Chronos, timers.size)
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
            Preferences.TIMER_LENGTH.set(this@Chronos, timers.size)
        }
        for (listener in listeners!!) {
            listener.onTimersChanged()
        }
    }

    internal var activityTheme: ThemeMode = ThemeMode.AUTO
        get() = ThemeMode.fromInt(Preferences.THEME.get(this))

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
