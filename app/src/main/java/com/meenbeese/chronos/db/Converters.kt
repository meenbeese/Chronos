package com.meenbeese.chronos.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromBooleanList(value: MutableList<Boolean>): String {
        return value.joinToString(separator = ",") { if (it) "1" else "0" }
    }

    @TypeConverter
    fun toBooleanList(value: String): MutableList<Boolean> {
        return value.split(",").mapTo(mutableListOf()) { it == "1" }
    }
}
