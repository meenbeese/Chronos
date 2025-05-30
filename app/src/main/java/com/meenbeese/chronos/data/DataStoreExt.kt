package com.meenbeese.chronos.data

import android.content.Context
import android.preference.PreferenceManager

import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context,
                PreferenceManager.getDefaultSharedPreferencesName(context)
            )
        )
    }
)
