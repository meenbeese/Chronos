package com.meenbeese.chronos.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable

import com.meenbeese.chronos.receivers.TimerReceiver

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

import kotlin.math.max

@Parcelize
class TimerData(
    var id: Int,
    var duration: Long = 600_000,
    var endTime: Long = 0,
    var isVibrate: Boolean = true,
    var sound: SoundData? = null
) : Parcelable {

    constructor(id: Int, context: Context) : this(id) {
        duration = Preferences.TIMER_DURATION.get(context).toLong()
        endTime = Preferences.TIMER_END_TIME.get(context)
        isVibrate = Preferences.TIMER_VIBRATE.get(context)

        val defaultSoundPref = Preferences.DEFAULT_TIMER_RINGTONE.get(context)
        val fallbackSound = Preferences.TIMER_SOUND.get(context)

        val defaultSound = defaultSoundPref
            .takeIf { it.isNotEmpty() }
            ?: fallbackSound.takeIf { it.isNotEmpty() }

        sound = defaultSound?.let { SoundData.fromString(it).getOrNull() }
    }

    val isSet: Boolean
        get() = endTime > System.currentTimeMillis()

    val remainingMillis: Long
        get() = max(endTime - System.currentTimeMillis(), 0)

    /**
     * Moves this TimerData's preferences to another "id".
     *
     * @param id            The new id to be assigned
     * @param context       An active context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onIdChanged(id: Int, context: Context) {
        GlobalScope.launch {
            Preferences.TIMER_DURATION.set(context, duration.toInt())
            Preferences.TIMER_END_TIME.set(context, endTime)
            Preferences.TIMER_VIBRATE.set(context, isVibrate)
            Preferences.TIMER_SOUND.set(context, sound?.toString() ?: "")
        }
        onRemoved(context)
        this.id = id
        if (isSet) set(context, context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
    }

    /**
     * Removes this TimerData's preferences.
     *
     * @param context       An active context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onRemoved(context: Context) {
        cancel(context, context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        GlobalScope.launch {
            Preferences.TIMER_DURATION.set(context, 0)
            Preferences.TIMER_END_TIME.set(context, 0L)
            Preferences.TIMER_VIBRATE.set(context, false)
            Preferences.TIMER_SOUND.set(context, "")
        }
    }

    /**
     * Set the duration of the timer.
     *
     * @param duration      The total length of the timer, in milliseconds.
     * @param context       An active Context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setDuration(duration: Long, context: Context) {
        this.duration = duration
        GlobalScope.launch { Preferences.TIMER_DURATION.set(context, duration.toInt()) }
    }

    /**
     * Set whether the timer should vibrate when it goes off.
     *
     * @param context       An active Context instance.
     * @param isVibrate     Whether the timer should vibrate.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setVibrate(context: Context, isVibrate: Boolean) {
        this.isVibrate = isVibrate
        GlobalScope.launch { Preferences.TIMER_VIBRATE.set(context, isVibrate) }
    }

    /**
     * Return whether the timer has a sound or not.
     *
     * @return              A boolean defining whether a sound has been set
     * for the timer.
     */
    fun hasSound(): Boolean = sound != null

    /**
     * Set the sound that the timer should make.
     *
     * @param context       An active context instance.
     * @param sound         A [SoundData](./SoundData) defining the sound that
     *                      the timer should make.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setSound(context: Context, sound: SoundData?) {
        this.sound = sound
        GlobalScope.launch { Preferences.TIMER_SOUND.set(context, sound?.toString() ?: "") }
    }

    /**
     * Set the next time for the timer to ring.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the timer on.
     */
    @OptIn(DelicateCoroutinesApi::class)
    operator fun set(context: Context, manager: AlarmManager) {
        endTime = System.currentTimeMillis() + duration
        setAlarm(context, manager)
        GlobalScope.launch { Preferences.TIMER_END_TIME.set(context, endTime) }
    }

    /**
     * Schedule a time for the alert to ring at.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager to schedule the alert on.
     */
    fun setAlarm(context: Context?, manager: AlarmManager) {
        manager.setExact(AlarmManager.RTC_WAKEUP, endTime, getIntent(context))
    }

    /**
     * Cancel the pending alert.
     *
     * @param context       An active context instance.
     * @param manager       The AlarmManager that the alert was scheduled on.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun cancel(context: Context, manager: AlarmManager) {
        endTime = 0
        manager.cancel(getIntent(context))
        GlobalScope.launch { Preferences.TIMER_END_TIME.set(context, endTime) }
    }

    /**
     * The intent to fire when the alert should ring.
     *
     * @param context       An active context instance.
     * @return              A PendingIntent that will open the alert screen.
     */
    private fun getIntent(context: Context?): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java).apply {
            putExtra(TimerReceiver.EXTRA_TIMER_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
