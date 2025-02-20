package com.meenbeese.chronos.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

import com.meenbeese.chronos.receivers.TimerReceiver

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import kotlin.math.max


open class TimerData : Parcelable {
    private var id: Int

    /**
     * The total length of the timer.
     *
     * @return              The total length of the timer, in milliseconds.
     */
    var duration: Long = 600000
        private set
    private var endTime: Long = 0
    @JvmField
    var isVibrate = true

    /**
     * Get the [SoundData](./SoundData) sound specified for the timer.
     *
     * @return              An instance of SoundData describing the sound that
     *                      the timer should make (or null).
     */
    @JvmField
    var sound: SoundData? = null

    constructor(id: Int) {
        this.id = id
    }

    constructor(id: Int, context: Context) {
        this.id = id
        duration = PreferenceData.TIMER_DURATION.getValue(context)
        endTime = PreferenceData.TIMER_END_TIME.getValue(context)
        isVibrate = PreferenceData.TIMER_VIBRATE.getValue(context)
        val defaultSound: String = if (PreferenceData.DEFAULT_TIMER_RINGTONE.getValue<String>(context).isNotEmpty()) {
            PreferenceData.DEFAULT_TIMER_RINGTONE.getValue(context)
        } else {
            PreferenceData.TIMER_SOUND.getValue(context)
        }
        sound = SoundData.fromString(defaultSound)
    }

    /**
     * Moves this TimerData's preferences to another "id".
     *
     * @param id            The new id to be assigned
     * @param context       An active context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onIdChanged(id: Int, context: Context) {
        GlobalScope.launch {
            PreferenceData.TIMER_DURATION.setValue(context, duration)
            PreferenceData.TIMER_END_TIME.setValue(context, endTime)
            PreferenceData.TIMER_VIBRATE.setValue(context, isVibrate)
            PreferenceData.TIMER_SOUND.setValue(context, if (sound != null) sound.toString() else null)
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
            PreferenceData.TIMER_DURATION.setValue(context, 0)
            PreferenceData.TIMER_END_TIME.setValue<Any>(context, 0L)
            PreferenceData.TIMER_VIBRATE.setValue<Any>(context, false)
            PreferenceData.TIMER_SOUND.setValue<Any>(context, "")
        }
    }

    val isSet: Boolean
        /**
         * Decides if the Timer has been set or should be ignored.
         *
         * @return              True if the timer should go off at some time in the future.
         */
        get() = endTime > System.currentTimeMillis()
    val remainingMillis: Long
        /**
         * Get the remaining amount of milliseconds before the timer should go off. This
         * may return a negative number.
         *
         * @return              The amount of milliseconds before the timer should go off.
         */
        get() = max(endTime - System.currentTimeMillis(), 0)

    /**
     * Set the duration of the timer.
     *
     * @param duration      The total length of the timer, in milliseconds.
     * @param context       An active Context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setDuration(duration: Long, context: Context) {
        this.duration = duration
        GlobalScope.launch { PreferenceData.TIMER_DURATION.setValue(context, duration) }
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
        GlobalScope.launch { PreferenceData.TIMER_VIBRATE.setValue(context, isVibrate) }
    }

    /**
     * Return whether the timer has a sound or not.
     *
     * @return              A boolean defining whether a sound has been set
     * for the timer.
     */
    fun hasSound(): Boolean {
        return sound != null
    }

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
        GlobalScope.launch { PreferenceData.TIMER_SOUND.setValue(context, sound?.toString()) }
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
        GlobalScope.launch { PreferenceData.TIMER_END_TIME.setValue(context, endTime) }
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
        GlobalScope.launch { PreferenceData.TIMER_END_TIME.setValue(context, endTime) }
    }

    /**
     * The intent to fire when the alert should ring.
     *
     * @param context       An active context instance.
     * @return              A PendingIntent that will open the alert screen.
     */
    private fun getIntent(context: Context?): PendingIntent {
        val intent = Intent(context, TimerReceiver::class.java)
        intent.putExtra(TimerReceiver.EXTRA_TIMER_ID, id)
        return PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeLong(duration)
        parcel.writeLong(endTime)
        parcel.writeByte((if (isVibrate) 1 else 0).toByte())
        parcel.writeByte((if (sound != null) 1 else 0).toByte())
        if (sound != null) parcel.writeString(sound.toString())
    }

    protected constructor(parcel: Parcel) {
        id = parcel.readInt()
        duration = parcel.readLong()
        endTime = parcel.readLong()
        isVibrate = parcel.readByte().toInt() != 0
        if (parcel.readByte().toInt() == 1) sound = SoundData.fromString(parcel.readString()!!)
    }

    companion object CREATOR : Creator<TimerData> {
        override fun createFromParcel(parcel: Parcel): TimerData {
            return TimerData(parcel)
        }

        override fun newArray(size: Int): Array<TimerData?> {
            return arrayOfNulls(size)
        }
    }
}
