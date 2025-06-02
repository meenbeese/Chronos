package com.meenbeese.chronos.data

import android.content.Context
import android.util.Log

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

import com.meenbeese.chronos.utils.Option
import com.meenbeese.chronos.utils.Theme

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

enum class PreferenceData(private val key: Preferences.Key<*>, private val defaultValue: Any?) {
    INFO_BACKGROUND_PERMISSIONS(booleanPreferencesKey(name = "info_background_permissions"), defaultValue = false),
    THEME(intPreferencesKey(name = "theme"), defaultValue = Theme.AUTO.value),
    BACKGROUND_IMAGE(stringPreferencesKey(name = "background_image"), defaultValue = "drawable/snowytrees"),
    RINGING_BACKGROUND_IMAGE(booleanPreferencesKey(name = "ringing_background_image"), defaultValue = true),
    TIMER_LENGTH(intPreferencesKey(name = "timer_length"), defaultValue = 0),
    DEFAULT_ALARM_RINGTONE(stringPreferencesKey(name = "default_alarm_ringtone"), defaultValue = null),
    DEFAULT_TIMER_RINGTONE(stringPreferencesKey(name = "default_timer_ringtone"), defaultValue = null),
    SLEEP_REMINDER(booleanPreferencesKey(name = "sleep_reminder"), defaultValue = true),
    SLEEP_REMINDER_TIME(longPreferencesKey(name = "sleep_reminder_time"), defaultValue = 25200000L),
    SLOW_WAKE_UP(booleanPreferencesKey(name = "slow_wake_up"), defaultValue = true),
    SLOW_WAKE_UP_TIME(longPreferencesKey(name = "slow_wake_up_time"), defaultValue = 300000L),
    SCROLL_TO_NEXT(booleanPreferencesKey(name = "scroll_to_next"), defaultValue = false),

    // Timer Settings
    TIMER_DURATION(intPreferencesKey(name = "timer_duration"), defaultValue = 600000),
    TIMER_END_TIME(longPreferencesKey(name = "timer_end_time"), defaultValue = 0L),
    TIMER_VIBRATE(booleanPreferencesKey(name = "timer_vibrate"), defaultValue = true),
    TIMER_SOUND(stringPreferencesKey(name = "timer_sound"), defaultValue = ""),

    // Time Format
    MILITARY_TIME(booleanPreferencesKey(name = "military_time"), defaultValue = false),

    // Time Zone
    TIME_ZONES(stringPreferencesKey(name = "time_zones"), defaultValue = ""),
    TIME_ZONE_ENABLED(booleanPreferencesKey(name = "time_zone_enabled"), defaultValue = false);

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(context: Context): T {
        return getOption<T>(context).getOrElse(defaultValue as T)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOption(context: Context): Option<T> {
        return runBlocking {
            val preferences = context.dataStore.data.first()
            val value = preferences[key] as? T
            if (value != null) Option.Some(value)
            else (defaultValue as? T)?.let { Option.Some(it) } ?: Option.None
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> setValue(context: Context, value: T) {
        context.dataStore.edit { settings ->
            when (value) {
                is Boolean -> settings[key as Preferences.Key<Boolean>] = value
                is String -> settings[key as Preferences.Key<String>] = value
                is Int -> settings[key as Preferences.Key<Int>] = value
                is Long -> settings[key as Preferences.Key<Long>] = value
                else -> {
                    Log.e("PreferenceData", "Attempted to store unsupported type: ${value?.javaClass?.name}")
                    throw IllegalArgumentException("Unsupported type: ${value?.javaClass?.name}")
                }
            }
        }
        Log.d("PreferenceData", "Saved $key = $value")
    }
}
