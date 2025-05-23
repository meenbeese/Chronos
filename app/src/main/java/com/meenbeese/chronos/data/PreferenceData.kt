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
    INFO_BACKGROUND_PERMISSIONS(booleanPreferencesKey(name = "info_background_permissions"), defaultValue = false),
    THEME(intPreferencesKey(name = "theme"), defaultValue = Chronos.THEME_AUTO),
    BACKGROUND_IMAGE(stringPreferencesKey(name = "background_image"), defaultValue = "https://jfenn.me/images/headers/snowytrees.jpg"),
    RINGING_BACKGROUND_IMAGE(booleanPreferencesKey(name = "ringing_background_image"), defaultValue = true),
    DAY_START(intPreferencesKey(name = "day_start"), defaultValue = 6),
    DAY_END(intPreferencesKey(name = "day_end"), defaultValue = 18),
    ALARM_LENGTH(intPreferencesKey(name = "alarm_length"), defaultValue = 0),
    TIMER_LENGTH(intPreferencesKey(name = "timer_length"), defaultValue = 0),
    DEFAULT_ALARM_RINGTONE(stringPreferencesKey(name = "default_alarm_ringtone"), defaultValue = null),
    DEFAULT_TIMER_RINGTONE(stringPreferencesKey(name = "default_timer_ringtone"), defaultValue = null),
    SLEEP_REMINDER(booleanPreferencesKey(name = "sleep_reminder"), defaultValue = true),
    SLEEP_REMINDER_TIME(longPreferencesKey(name = "sleep_reminder_time"), defaultValue = 25200000L),
    SLOW_WAKE_UP(booleanPreferencesKey(name = "slow_wake_up"), defaultValue = true),
    SLOW_WAKE_UP_TIME(longPreferencesKey(name = "slow_wake_up_time"), defaultValue = 300000L),

    // Timer Settings
    TIMER_DURATION(intPreferencesKey(name = "timer_duration"), defaultValue = 600000),
    TIMER_END_TIME(longPreferencesKey(name = "timer_end_time"), defaultValue = 0L),
    TIMER_VIBRATE(booleanPreferencesKey(name = "timer_vibrate"), defaultValue = true),
    TIMER_SOUND(stringPreferencesKey(name = "timer_sound"), defaultValue = ""),

    // Time Zone
    TIME_ZONE_ENABLED(booleanPreferencesKey(name = "time_zone_enabled"), defaultValue = false);

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
