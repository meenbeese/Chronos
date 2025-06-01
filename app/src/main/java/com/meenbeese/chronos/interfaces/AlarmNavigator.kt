package com.meenbeese.chronos.interfaces

interface AlarmNavigator {
    fun jumpToAlarm(alarmId: Int, openEditor: Boolean = true)
}
