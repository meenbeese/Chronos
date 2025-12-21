package com.meenbeese.chronos.ui.theme

import java.util.Calendar

enum class ThemeMode(val value: Int) {
    AUTO(0),
    DAY(1),
    NIGHT(2),
    AMOLED(3);

    companion object {
        fun fromInt(value: Int): ThemeMode = entries.find { it.value == value } ?: AUTO
    }

    fun isDark(currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
        return when (this) {
            NIGHT, AMOLED -> true
            DAY -> false
            AUTO -> currentHour !in 6..<18
        }
    }
}
