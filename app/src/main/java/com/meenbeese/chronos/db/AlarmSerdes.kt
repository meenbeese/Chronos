package com.meenbeese.chronos.db

import android.content.Context

import kotlinx.serialization.json.Json

class AlarmSerdes {
    suspend fun importAlarmDataFromJson(context: Context, json: String): Int {
        val db = AlarmDatabase.getDatabase(context)
        val oldAlarms = db.alarmDao().getAllAlarmsDirect()
        val newAlarms = Json.decodeFromString<List<AlarmEntity>>(json)

        db.alarmDao().apply {
            oldAlarms.forEach { delete(it) }
            newAlarms.forEach { insert(it) }
        }

        return newAlarms.size
    }

    suspend fun exportAlarmDataAsJson(context: Context): Pair<String, Int> {
        val db = AlarmDatabase.getDatabase(context)
        val alarms = db.alarmDao().getAllAlarmsDirect()
        val json = Json { prettyPrint = true }

        return json.encodeToString(alarms) to alarms.size
    }
}
