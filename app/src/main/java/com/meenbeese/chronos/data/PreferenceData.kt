package com.meenbeese.chronos.data

import android.content.Context

import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meenbeese.chronos.Chronos

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "old_prefs_name"))
    }
)


enum class PreferenceData(private val key: Preferences.Key<*>, private val defaultValue: Any?) {
    INFO_BACKGROUND_PERMISSIONS(booleanPreferencesKey("info_background_permissions"), false),
    THEME(intPreferencesKey("theme"), Chronos.THEME_AUTO),
    BACKGROUND_IMAGE(stringPreferencesKey("background_image"), "https://jfenn.me/images/headers/snowytrees.jpg"),
    RINGING_BACKGROUND_IMAGE(booleanPreferencesKey("ringing_background_image"), true),
    DAY_START(intPreferencesKey("day_start"), 6), // TODO: Change to minutes if needed
    DAY_END(intPreferencesKey("day_end"), 18),
    ALARM_LENGTH(intPreferencesKey("alarm_length"), 0),
    TIMER_LENGTH(intPreferencesKey("timer_length"), 0),
    DEFAULT_ALARM_RINGTONE(stringPreferencesKey("default_alarm_ringtone"), null),
    DEFAULT_TIMER_RINGTONE(stringPreferencesKey("default_timer_ringtone"), null),
    SLEEP_REMINDER(booleanPreferencesKey("sleep_reminder"), true),
    SLEEP_REMINDER_TIME(longPreferencesKey("sleep_reminder_time"), 25200000L),
    SLOW_WAKE_UP(booleanPreferencesKey("slow_wake_up"), true),
    SLOW_WAKE_UP_TIME(longPreferencesKey("slow_wake_up_time"), 300000L),

    // Alarm Settings
    ALARM_NAME(stringPreferencesKey("alarm_name"), "%d/ALARM_NAME"),
    ALARM_TIME(longPreferencesKey("alarm_time"), 0L),
    ALARM_ENABLED(booleanPreferencesKey("alarm_enabled"), true),
    ALARM_DAY_ENABLED(booleanPreferencesKey("alarm_day_enabled"), false),
    ALARM_VIBRATE(booleanPreferencesKey("alarm_vibrate"), true),
    ALARM_SOUND(stringPreferencesKey("alarm_sound"), ""),

    // Timer Settings
    TIMER_DURATION(intPreferencesKey("timer_duration"), 600000),
    TIMER_END_TIME(longPreferencesKey("timer_end_time"), 0L),
    TIMER_VIBRATE(booleanPreferencesKey("timer_vibrate"), true),
    TIMER_SOUND(stringPreferencesKey("timer_sound"), ""),

    // Time Zone
    TIME_ZONE_ENABLED(booleanPreferencesKey("time_zone_enabled"), false);

    @Suppress("UNCHECKED_CAST")
    fun <T> getValue(context: Context): T {
        return runBlocking {
            val preferences = context.dataStore.data.first()
            preferences[key] as? T ?: defaultValue as T
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
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }
}
