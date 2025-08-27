package com.meenbeese.chronos.db

import android.content.Context

import arrow.core.Either
import arrow.core.raise.either

import kotlinx.serialization.json.Json

class AlarmSerdes {
    private val jsonFormatter = Json { prettyPrint = true }

    suspend fun importAlarmDataFromJson(
        context: Context,
        json: String
    ): Either<Throwable, Int> = either {
        val db = AlarmDatabase.getDatabase(context)
        val oldAlarms = db.alarmDao().getAllAlarmsDirect()
        val newAlarms = Either.catch {
            jsonFormatter.decodeFromString<List<AlarmEntity>>(json)
        }.bind()

        db.alarmDao().apply {
            oldAlarms.forEach { delete(it) }
            newAlarms.forEach { insert(it) }
        }

        newAlarms.size
    }

    suspend fun exportAlarmDataAsJson(
        context: Context
    ): Either<Throwable, Pair<String, Int>> = either {
        val db = AlarmDatabase.getDatabase(context)
        val alarms = db.alarmDao().getAllAlarmsDirect()
        val json = Either.catch {
            jsonFormatter.encodeToString(alarms)
        }.bind()

        json to alarms.size
    }
}
