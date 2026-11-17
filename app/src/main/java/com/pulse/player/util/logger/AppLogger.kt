package com.pulse.player.util.logger

import android.util.Log
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.data.local.db.entity.ErrorLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

object AppLogger {
    private const val TAG = "PulseLogger"
    private var errorLogDao: ErrorLogDao? = null
    private val loggerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun initialize(dao: ErrorLogDao) {
        this.errorLogDao = dao
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        persistLog("INFO", tag, message, null)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
        persistLog("WARN", tag, message, throwable?.let(::getStackTraceString))
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        persistLog("ERROR", tag, message, throwable?.let(::getStackTraceString))
    }

    fun fatal(tag: String, message: String, throwable: Throwable) {
        Log.e(tag, "FATAL: $message", throwable)
        persistLog("FATAL", tag, message, getStackTraceString(throwable))
    }

    fun persistDirect(level: String, tag: String, message: String, stackTrace: String?) {
        try {
            errorLogDao?.let { dao ->
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    dao.insertLog(
                        ErrorLogEntity(
                            level = level,
                            tag = tag,
                            message = message,
                            stackTrace = stackTrace
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed direct persist to Room", e)
        }
    }

    private fun persistLog(level: String, tag: String, message: String, stackTrace: String?) {
        loggerScope.launch {
            try {
                errorLogDao?.insertLog(
                    ErrorLogEntity(
                        level = level,
                        tag = tag,
                        message = message,
                        stackTrace = stackTrace
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist log to SQLite", e)
            }
        }
    }

    fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}
