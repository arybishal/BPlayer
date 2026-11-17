package com.pulse.player.util.logger

import android.os.Process
import kotlin.system.exitProcess

class CrashHandler(
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            AppLogger.persistDirect(
                level = "FATAL",
                tag = "UncaughtException",
                message = "Thread [${thread.name}] panicked: ${throwable.message ?: throwable.javaClass.simpleName}",
                stackTrace = AppLogger.getStackTraceString(throwable)
            )
        } catch (_: Exception) {
            // Do not crash the crash handler
        } finally {
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }
}
