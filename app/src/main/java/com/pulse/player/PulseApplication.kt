package com.pulse.player

import android.app.Application
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.util.logger.AppLogger
import com.pulse.player.util.logger.CrashHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PulseApplication : Application() {

    @Inject
    lateinit var errorLogDao: ErrorLogDao

    override fun onCreate() {
        super.onCreate()

        // Initialize structured local SQLite logger
        AppLogger.initialize(errorLogDao)

        // Register global uncaught crash handler
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(previousHandler))

        AppLogger.i("PulseApp", "Pulse Application initialized successfully.")
    }
}
