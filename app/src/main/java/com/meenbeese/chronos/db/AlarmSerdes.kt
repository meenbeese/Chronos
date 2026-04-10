package com.meenbeese.chronos.db

import android.content.Context

import arrow.core.Either
import arrow.core.raise.either

import androidx.room.withTransaction

import kotlinx.serialization.json.Json

class AlarmSerdes {
    private val jsonFormatter = Json { prettyPrint = false }

    suspend fun importAlarmDataFromJson(
        context: Context,
        json: String
    ): Either<Throwable, Int> = either {
        val db = AlarmDatabase.getDatabase(context)
        val newAlarms = Either.catch {
            jsonFormatter.decodeFromString<List<AlarmEntity>>(json)
        }.bind()

        db.withTransaction {
            val dao = db.alarmDao()
            val oldAlarms = dao.getAllAlarmsDirect()
            oldAlarms.forEach { dao.delete(it) }
            newAlarms.forEach { dao.insert(it) }
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
