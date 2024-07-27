package com.meenbeese.chronos.utils

import android.content.Context
import android.text.format.DateFormat

import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H


object LocaleUtils {
    @JvmStatic
    fun getLocaleClockFormat(): Int {
        val context: Context = CoreHelper.contextGetter.invoke()
        val isSystem24Hour = DateFormat.is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) CLOCK_24H else CLOCK_12H
        return clockFormat
    }
}
