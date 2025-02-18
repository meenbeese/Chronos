package com.meenbeese.chronos.data

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

import com.meenbeese.chronos.R
import com.meenbeese.chronos.activities.MainActivity
import com.meenbeese.chronos.data.SoundData.Companion.fromString
import com.meenbeese.chronos.receivers.AlarmReceiver
import com.meenbeese.chronos.services.SleepReminderService
import com.meenbeese.chronos.services.SleepReminderService.Companion.refreshSleepTime

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.util.Calendar
import java.util.Date


class AlarmData(
    var id: Int,
    var name: String? = null,
    var time: Calendar = Calendar.getInstance()
) : Parcelable {
    var isEnabled: Boolean = true
    var days: BooleanArray = BooleanArray(7)
    var isVibrate: Boolean = true

    /**
     * Get the [SoundData](./SoundData) sound specified for the alarm.
     *
     * @return              An instance of SoundData describing the sound that
     * the alarm should make (or null).
     */
    var sound: SoundData? = null

    constructor(id: Int, context: Context) : this(id) {
        name = PreferenceData.ALARM_NAME.getValue(context)
        time.timeInMillis = PreferenceData.ALARM_TIME.getValue(context)
        isEnabled = PreferenceData.ALARM_ENABLED.getValue(context)
        days = BooleanArray(7) { PreferenceData.ALARM_DAY_ENABLED.getValue(context) }
        isVibrate = PreferenceData.ALARM_VIBRATE.getValue(context)
        val defaultSound: String = if (PreferenceData.DEFAULT_ALARM_RINGTONE.getValue<String>(context).isNotEmpty()) {
            PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(context)
        } else {
            PreferenceData.ALARM_SOUND.getValue(context)
        }
        sound = fromString(defaultSound)
    }

    /**
     * Moves this AlarmData's preferences to another "id".
     *
     * @param newId         The new id to be assigned
     * @param context       An active context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onIdChanged(newId: Int, context: Context) {
        GlobalScope.launch {
            PreferenceData.ALARM_NAME.setValue(context, getName(context))
            PreferenceData.ALARM_TIME.setValue(context, time.timeInMillis)
            PreferenceData.ALARM_ENABLED.setValue(context, isEnabled)
            days.forEachIndexed { _, enabled -> PreferenceData.ALARM_DAY_ENABLED.setValue(context, enabled) }
            PreferenceData.ALARM_VIBRATE.setValue(context, isVibrate)
            PreferenceData.ALARM_SOUND.setValue(context, sound?.toString())
        }

        onRemoved(context)
        id = newId
        if (isEnabled) set(context)
    }

    /**
     * Removes this AlarmData's preferences.
     *
     * @param context       An active context instance.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun onRemoved(context: Context) {
        cancel(context)

        GlobalScope.launch {
            PreferenceData.ALARM_NAME.setValue(context, null)
            PreferenceData.ALARM_TIME.setValue(context, null)
            PreferenceData.ALARM_ENABLED.setValue(context, null)
            days.indices.forEach { _ -> PreferenceData.ALARM_DAY_ENABLED.setValue(context, null) }
            PreferenceData.ALARM_VIBRATE.setValue(context, null)
            PreferenceData.ALARM_SOUND.setValue(context, null)
        }
    }

    fun getName(context: Context): String = name ?: context.getString(R.string.title_alarm, id + 1)

    fun isRepeat(): Boolean = days.any { it }

    /**
     * Sets the user-defined "name" of the alarm.
     *
     * @param context       An active context instance.
     * @param name          The new name to be set.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setName(context: Context, name: String?) {
        this.name = name
        GlobalScope.launch {
            PreferenceData.ALARM_NAME.setValue(context, name)
        }
    }

    /**
     * Change the scheduled alarm time.
     *
     * @param context       An active context instance.
     * @param timeMillis    The UNIX time (in milliseconds) that the alarm should ring at.
     * This is independent of days; if the time correlates to 9:30 on
     * a Tuesday when the alarm should only repeat on Wednesdays and
     * Thursdays, then the alarm will next ring at 9:30 on Wednesday.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setTime(context: Context, timeMillis: Long) {
        time.timeInMillis = timeMillis
        GlobalScope.launch {
            PreferenceData.ALARM_TIME.setValue(context, timeMillis)
        }
        if (isEnabled) set(context)
    }

    /**
     * Set whether the alarm is enabled.
     *
     * @param context       An active context instance.
     * @param enabled       Whether the alarm is enabled.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setEnabled(context: Context, enabled: Boolean) {
        isEnabled = enabled
        GlobalScope.launch {
            PreferenceData.ALARM_ENABLED.setValue(context, enabled)
        }
        if (enabled) set(context) else cancel(context)
    }

    /**
     * Sets the days of the week that the alarm should ring on. If
     * no days are specified, the alarm will act as a one-time alert
     * and will not repeat.
     *
     * @param context       An active context instance.
     * @param newDays       A boolean array, with a length of 7 (seven days of the week)
     * specifying whether repeat is enabled for that day.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setDays(context: Context, newDays: BooleanArray) {
        days = newDays
        GlobalScope.launch {
            days.indices.forEach { PreferenceData.ALARM_DAY_ENABLED.setValue(context, newDays[it]) }
        }
    }

    /**
     * Set whether the alarm should vibrate.
     *
     * @param context       An active context instance.
     * @param vibrate       Whether the alarm should vibrate.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setVibrate(context: Context, vibrate: Boolean) {
        isVibrate = vibrate
        GlobalScope.launch {
            PreferenceData.ALARM_VIBRATE.setValue(context, vibrate)
        }
    }

    /**
     * Return whether the alarm has a sound or not.
     *
     * @return              A boolean defining whether a sound has been set
     * for the alarm.
     */
    fun hasSound(): Boolean = sound != null

    /**
     * Set the sound that the alarm should make.
     *
     * @param context       An active context instance.
     * @param newSound      A [SoundData](./SoundData) defining the sound that
     * the alarm should make.
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun setSound(context: Context, newSound: SoundData?) {
        sound = newSound
        GlobalScope.launch {
            PreferenceData.ALARM_SOUND.setValue(context, newSound?.toString())
        }
    }

    /**
     * Get the next time that the alarm should wring.
     *
     * @return              A Calendar object defining the next time that the alarm should ring at.
     * @see [java.util.Calendar Documentation]
     */
    fun getNext(): Calendar? {
        if (!isEnabled) return null
        val now = Calendar.getInstance()
        val next = time.clone() as Calendar
        next.set(Calendar.SECOND, 0)
        while (now.after(next)) next.add(Calendar.DATE, 1)
        if (isRepeat()) {
            var nextDay = next.get(Calendar.DAY_OF_WEEK) - 1
            repeat(7) {
                if (days[nextDay]) return@repeat
                nextDay = (nextDay + 1) % 7
            }
            next.set(Calendar.DAY_OF_WEEK, nextDay + 1)
            while (now.after(next)) next.add(Calendar.DATE, 7)
        }
        return next
    }

    /**
     * Set the next time for the alarm to ring.
     *
     * @param context       An active context instance.
     * @return              The next [Date]([...](https://developer.android.com/reference/java/util/Date))
     * at which the alarm will ring.
     */
    fun set(context: Context): Date? {
        val nextTime = getNext() ?: return null
        setAlarm(context, nextTime.timeInMillis)
        return nextTime.time
    }

    /**
     * Schedule a time for the alarm to ring at.
     *
     * @param context       An active context instance.
     * @param timeMillis    A UNIX timestamp specifying the next time for the alarm to ring.
     */
    private fun setAlarm(context: Context, timeMillis: Long) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setAlarmClock(
            AlarmClockInfo(timeMillis, PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )),
            getIntent(context)
        )
        manager.set(
            AlarmManager.RTC_WAKEUP,
            timeMillis - PreferenceData.SLEEP_REMINDER_TIME.getValue(context) as Long,
            PendingIntent.getService(
                context, 0, Intent(context, SleepReminderService::class.java), PendingIntent.FLAG_IMMUTABLE
            )
        )
        refreshSleepTime(context)
    }

    /**
     * Cancel the next time for the alarm to ring.
     *
     * @param context       An active context instance.
     */
    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(getIntent(context))
    }

    /**
     * The intent to fire when the alarm should ring.
     *
     * @param context       An active context instance.
     * @return              A PendingIntent that will open the alert screen.
     */
    private fun getIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        }
        return PendingIntent.getBroadcast(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        name = parcel.readString()
        time.timeInMillis = parcel.readLong()
        isEnabled = parcel.readByte() != 0.toByte()
        days = BooleanArray(7).apply { parcel.readBooleanArray(this) }
        isVibrate = parcel.readByte() != 0.toByte()
        sound = parcel.readString()?.let { fromString(it) }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeLong(time.timeInMillis)
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeBooleanArray(days)
        parcel.writeByte(if (isVibrate) 1 else 0)
        parcel.writeString(sound?.toString())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AlarmData> {
        override fun createFromParcel(parcel: Parcel): AlarmData = AlarmData(parcel)
        override fun newArray(size: Int): Array<AlarmData?> = arrayOfNulls(size)
    }
}
