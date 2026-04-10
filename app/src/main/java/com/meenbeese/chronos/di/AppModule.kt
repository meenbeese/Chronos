package com.meenbeese.chronos.di

import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.db.AlarmDatabase
import com.meenbeese.chronos.db.AlarmRepository
import com.meenbeese.chronos.db.TimerAlarmRepository
import com.meenbeese.chronos.utils.MediaManager

import org.koin.dsl.module
import org.koin.dsl.onClose

@UnstableApi
val appModule = module {
    single { MediaManager(get()) }
    single { AlarmDatabase.getDatabase(get()) }
    single { get<AlarmDatabase>().alarmDao() }
    single { AlarmRepository(get()) }
    single { TimerAlarmRepository(get(), get()) } onClose { it?.clear() }
}
