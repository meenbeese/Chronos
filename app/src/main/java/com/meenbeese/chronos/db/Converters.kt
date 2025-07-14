package com.meenbeese.chronos.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromBooleanList(value: List<Boolean>): String {
        return value.joinToString(separator = ",") { if (it) "1" else "0" }
    }

    @TypeConverter
    fun toBooleanList(value: String): List<Boolean> {
        return value.split(",").map { it == "1" }
    }
}
