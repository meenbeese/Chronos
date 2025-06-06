package com.meenbeese.chronos.data

import android.graphics.Color

import com.meenbeese.chronos.utils.Theme

object Preferences {
    // Background
    val COLORFUL_BACKGROUND = PreferenceEntry.BooleanPref(name = "colorful_background", default = false)
    val BACKGROUND_IMAGE = PreferenceEntry.StringPref(name = "background_image", default = "drawable/snowytrees")
    val BACKGROUND_COLOR = PreferenceEntry.IntPref(name = "background_color", default = Color.WHITE)
    val RINGING_BACKGROUND_IMAGE = PreferenceEntry.BooleanPref(name = "ringing_background_image", default = true)

    // Sleep
    val SLEEP_REMINDER = PreferenceEntry.BooleanPref(name = "sleep_reminder", default = true)
    val SLEEP_REMINDER_TIME = PreferenceEntry.LongPref(name = "sleep_reminder_time", default = 25200000L)

    // Ringtone
    val DEFAULT_ALARM_RINGTONE = PreferenceEntry.StringPref(name = "default_alarm_ringtone", default = "")
    val DEFAULT_TIMER_RINGTONE = PreferenceEntry.StringPref(name = "default_timer_ringtone", default = "")

    // Wake Up
    val SLOW_WAKE_UP = PreferenceEntry.BooleanPref(name = "slow_wake_up", default = true)
    val SLOW_WAKE_UP_TIME = PreferenceEntry.LongPref(name = "slow_wake_up_time", default = 300000L)

    // Timer Settings
    val TIMER_DURATION = PreferenceEntry.IntPref(name = "timer_duration", default = 600000)
    val TIMER_END_TIME = PreferenceEntry.LongPref(name = "timer_end_time", default = 0L)
    val TIMER_VIBRATE = PreferenceEntry.BooleanPref(name = "timer_vibrate", default = true)
    val TIMER_SOUND = PreferenceEntry.StringPref(name = "timer_sound", default = "")

    // Time Format
    val MILITARY_TIME = PreferenceEntry.BooleanPref(name = "military_time", default = false)

    // Time Zone
    val TIME_ZONES = PreferenceEntry.StringPref(name = "time_zones", default = "")
    val TIME_ZONE_ENABLED = PreferenceEntry.BooleanPref(name = "time_zone_enabled", default = false)

    // Other
    val INFO_BACKGROUND_PERMISSIONS = PreferenceEntry.BooleanPref(name = "info_background_permissions", default = false)
    val THEME = PreferenceEntry.IntPref(name = "theme", default = Theme.AUTO.value)
    val TIMER_LENGTH = PreferenceEntry.IntPref(name = "timer_length", default = 0)
    val SCROLL_TO_NEXT = PreferenceEntry.BooleanPref(name = "scroll_to_next", default = false)
}
