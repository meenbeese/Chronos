package com.meenbeese.chronos.db

import androidx.lifecycle.LiveData

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmRepository(private val alarmDao: AlarmDao) {
    suspend fun insert(alarm: AlarmEntity): Long = withContext(Dispatchers.IO) {
        alarmDao.insert(alarm)
    }

    suspend fun update(alarm: AlarmEntity) = withContext(Dispatchers.IO) {
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: AlarmEntity) = withContext(Dispatchers.IO) {
        alarmDao.delete(alarm)
    }

    suspend fun getAllDirect(): List<AlarmEntity> = withContext(Dispatchers.IO) {
        alarmDao.getAllAlarmsDirect()
    }

    fun getAll(): LiveData<List<AlarmEntity>> {
        return alarmDao.getAllAlarms()
    }

    suspend fun saveAlarm(
        id: Int,
        name: String?,
        timeInMillis: Long,
        isEnabled: Boolean,
        days: MutableList<Boolean>,
        isVibrate: Boolean,
        sound: String?
    ) = withContext(Dispatchers.IO) {
        val entity = AlarmEntity(id, name, timeInMillis, isEnabled, days, isVibrate, sound)
        if (alarmDao.getAlarmById(id) == null) {
            alarmDao.insert(entity)
        } else {
            alarmDao.update(entity)
        }
    }
}
