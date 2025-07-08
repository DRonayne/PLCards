package com.darach.plcards

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PLCardsApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Configure Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("debug_build", BuildConfig.DEBUG)
        }
    }
}