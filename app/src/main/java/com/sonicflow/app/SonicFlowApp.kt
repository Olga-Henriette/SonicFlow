package com.sonicflow.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SonicFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialiser Timber pour les logs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("SonicFlow App Started")
    }
}