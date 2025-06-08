package com.meenbeese.chronos.di

import androidx.media3.common.util.UnstableApi

import com.meenbeese.chronos.utils.AudioUtils

import org.koin.dsl.module

@UnstableApi
val appModule = module {
    single { AudioUtils(get()) }
}
