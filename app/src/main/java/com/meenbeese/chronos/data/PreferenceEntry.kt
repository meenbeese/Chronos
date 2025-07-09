package com.meenbeese.chronos.data

import android.content.Context

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

import com.meenbeese.chronos.ext.dataStore

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

sealed class PreferenceEntry<T>(
    val key: Preferences.Key<T>,
    val defaultValue: T
) {
    abstract fun get(context: Context): T
    abstract suspend fun set(context: Context, value: T)

    class BooleanPref(name: String, default: Boolean) :
        PreferenceEntry<Boolean>(booleanPreferencesKey(name), default) {
        override fun get(context: Context) = runBlocking { context.dataStore.data.first()[key] ?: defaultValue }
        override suspend fun set(context: Context, value: Boolean) {
            context.dataStore.edit { it[key] = value }
        }
    }

    class IntPref(name: String, default: Int) :
        PreferenceEntry<Int>(intPreferencesKey(name), default) {
        override fun get(context: Context) = runBlocking { context.dataStore.data.first()[key] ?: defaultValue }
        override suspend fun set(context: Context, value: Int) {
            context.dataStore.edit { it[key] = value }
        }
    }

    class LongPref(name: String, default: Long) :
        PreferenceEntry<Long>(longPreferencesKey(name), default) {
        override fun get(context: Context) = runBlocking { context.dataStore.data.first()[key] ?: defaultValue }
        override suspend fun set(context: Context, value: Long) {
            context.dataStore.edit { it[key] = value }
        }
    }

    class StringPref(name: String, default: String) :
        PreferenceEntry<String>(stringPreferencesKey(name), default) {
        override fun get(context: Context) = runBlocking { context.dataStore.data.first()[key] ?: defaultValue }
        override suspend fun set(context: Context, value: String) {
            context.dataStore.edit { it[key] = value }
        }
    }
}
