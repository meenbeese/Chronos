package com.meenbeese.chronos.utils

import android.content.Context
import android.text.format.DateFormat

import com.meenbeese.chronos.R

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {
    private const val FORMAT_12H = "h:mm:ss"
    private const val FORMAT_24H = "HH:mm:ss"
    private const val FORMAT_12H_SHORT = "h:mm a"
    private const val FORMAT_24H_SHORT = "HH:mm"
    const val FORMAT_DATE = "MMMM d yyyy"

    /**
     * Get the proper hh:mm:ss time format to use, dependent on whether
     * 24-hour time is enabled in the system settings.
     *
     * @param context       An active context instance.
     * @return              A string to format hh:mm:ss time.
     */
    private fun getFormat(context: Context?): String {
        return if (DateFormat.is24HourFormat(context)) FORMAT_24H else FORMAT_12H
    }

    /**
     * A shorter version of [getFormat](#getformat) with the AM/PM indicator
     * in the 12-hour version.
     *
     * @param context       An active context instance.
     * @return              A string to format hh:mm time.
     */
    @JvmStatic
    fun getShortFormat(context: Context?): String {
        return if (DateFormat.is24HourFormat(context)) FORMAT_24H_SHORT else FORMAT_12H_SHORT
    }

    /**
     * Formats the provided time into a string using [getFormat](#getformat).
     *
     * @param context       An active context instance.
     * @param time          The time to be formatted.
     * @return              A formatted hh:mm:ss string.
     */
    fun format(context: Context?, time: Date?): String {
        return format(time, getFormat(context))
    }

    /**
     * Formats the provided time into a string using [getShortFormat](#getshortformat).
     *
     * @param context       An active context instance.
     * @param time          The time to be formatted.
     * @return              A formatted hh:mm string.
     */
    fun formatShort(context: Context?, time: Date?): String {
        return format(time, getShortFormat(context))
    }

    /**
     * Formats the provided time into the provided format.
     *
     * @param time          The time to be formatted.
     * @param format        The format to format the time into.
     * @return              The formatted time string.
     */
    @JvmStatic
    fun format(time: Date?, format: String?): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(time!!)
    }

    /**
     * Formats a duration of milliseconds into a "0h 00m 00s 00" string.
     *
     * @param millis        The millisecond duration to be formatted.
     * @return              The formatted time string.
     */
    @JvmStatic
    fun formatMillis(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        val micros = TimeUnit.MILLISECONDS.toMicros(millis) % TimeUnit.SECONDS.toMicros(1) / 10000
        return if (hours > 0) String.format(
            Locale.getDefault(),
            "%dh %02dm %02ds %02d",
            hours,
            minutes,
            seconds,
            micros
        ) else if (minutes > 0) String.format(
            Locale.getDefault(),
            "%dm %02ds %02d",
            minutes,
            seconds,
            micros
        ) else String.format(Locale.getDefault(), "%ds %02d", seconds, micros)
    }

    /**
     * Formats a duration of minutes into a meaningful string to be used in
     * idk maybe a sentence or something. An input of 60 becomes "1 hour", 59
     * becomes "59 minutes", and so on.
     *
     * @param context       An active context instance.
     * @param minutes       The duration of minutes to format.
     * @return              The formatted time string.
     */
    @JvmStatic
    fun formatUnit(context: Context, minutes: Int): String {
        var mins = minutes
        val days = TimeUnit.MINUTES.toDays(mins.toLong())
        val hours = TimeUnit.MINUTES.toHours(mins.toLong()) % TimeUnit.DAYS.toHours(1)
        mins %= TimeUnit.HOURS.toMinutes(1).toInt()
        return if (days > 0) String.format(
            Locale.getDefault(),
            "%d " + context.getString(if (days > 1) R.string.word_days else R.string.word_day) + ", %d " + context.getString(
                if (hours > 1) R.string.word_hours else R.string.word_hour
            ) + if (mins > 0) ", " + context.getString(R.string.word_join) + " %d " + context.getString(
                if (mins > 1) R.string.word_minutes else R.string.word_minute
            ) else "",
            days,
            hours,
            mins
        ) else if (hours > 0) String.format(
            Locale.getDefault(),
            "%d " + context.getString(if (hours > 1) R.string.word_hours else R.string.word_hour) + if (mins > 0) " " + context.getString(
                R.string.word_join
            ) + " %d " + context.getString(if (mins > 1) R.string.word_minutes else R.string.word_minute) else "",
            hours,
            mins
        ) else String.format(
            Locale.getDefault(),
            "%d " + context.getString(if (mins > 1) R.string.word_minutes else R.string.word_minute),
            mins
        )
    }
}
