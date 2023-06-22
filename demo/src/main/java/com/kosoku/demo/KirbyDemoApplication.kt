package com.kosoku.demo

import android.app.Application
import timber.log.Timber

class KirbyDemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}