package com.meenbeese.chronos.utils

enum class Theme(val value: Int) {
    AUTO(0),
    DAY(1),
    NIGHT(2),
    AMOLED(3);

    companion object {
        fun fromInt(value: Int): Theme = Theme.entries.find { it.value == value } ?: AUTO
    }
}
