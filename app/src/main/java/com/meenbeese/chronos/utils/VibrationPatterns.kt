package com.meenbeese.chronos.utils

object VibrationPatterns {
    const val DEFAULT = "default"
    const val PULSE = "pulse"
    const val HEARTBEAT = "heartbeat"
    const val RAPID = "rapid"

    fun patternFor(key: String): LongArray {
        return when (key) {
            PULSE -> longArrayOf(0, 200, 200)
            HEARTBEAT -> longArrayOf(0, 150, 100, 150, 700)
            RAPID -> longArrayOf(0, 100, 100)
            else -> longArrayOf(0, 500, 500)
        }
    }
}
