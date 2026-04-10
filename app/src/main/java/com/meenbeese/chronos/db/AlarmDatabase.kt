package com.meenbeese.chronos.db

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val DATABASE_NAME = "alarm_database"

        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val deviceContext = appContext.createDeviceProtectedStorageContext()
                if (!deviceContext.databaseList().contains(DATABASE_NAME) &&
                    appContext.databaseList().contains(DATABASE_NAME)
                ) {
                    deviceContext.moveDatabaseFrom(appContext, DATABASE_NAME)
                }

                val instance = Room.databaseBuilder(
                    deviceContext,
                    AlarmDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
