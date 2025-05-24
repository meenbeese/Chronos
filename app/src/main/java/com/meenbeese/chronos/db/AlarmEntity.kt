package com.meenbeese.chronos.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?,
    val timeInMillis: Long,
    val isEnabled: Boolean,
    val days: MutableList<Boolean>,
    val isVibrate: Boolean,
    val sound: String?
)
