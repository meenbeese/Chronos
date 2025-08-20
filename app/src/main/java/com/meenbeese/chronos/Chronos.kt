package com.meenbeese.chronos

import android.app.Application

import com.meenbeese.chronos.di.appModule

import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Chronos : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Chronos)
            modules(appModule)
        }
    }
}
