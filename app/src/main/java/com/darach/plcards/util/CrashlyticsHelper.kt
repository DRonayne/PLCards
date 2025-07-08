package com.darach.plcards.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsHelper @Inject constructor(
    private val crashlytics: FirebaseCrashlytics
) {

    fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}