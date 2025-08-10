package com.featurevisor.sdk

import com.featurevisor.sdk.Logger.LogLevel.DEBUG
import com.featurevisor.sdk.Logger.LogLevel.ERROR
import com.featurevisor.sdk.Logger.LogLevel.INFO
import com.featurevisor.sdk.Logger.LogLevel.WARN

typealias LogDetails = Map<String, Any>
typealias LogHandler = (level: Logger.LogLevel, message: String, details: LogDetails?) -> Unit

class Logger(
    private var logLevel: LogLevel,
    private val handle: LogHandler,
) {
    companion object {
        private val defaultLogLevel = LogLevel.WARN
        private val defaultLogHandler: LogHandler = { level, message, _ ->
            println("[${level.value}] $message")
        }

        fun createLogger(
            level: LogLevel = defaultLogLevel,
            handle: LogHandler = defaultLogHandler,
        ): Logger {
            return Logger(level, handle)
        }
    }

    fun setLevel(level: LogLevel) {
        this.logLevel = level
    }

    fun debug(message: String, details: LogDetails? = null) {
        log(LogLevel.DEBUG, message, details)
    }

    fun info(message: String, details: LogDetails? = null) {
        log(LogLevel.INFO, message, details)
    }

    fun warn(message: String, details: LogDetails? = null) {
        log(LogLevel.WARN, message, details)
    }

    fun error(message: String, details: LogDetails? = null) {
        log(LogLevel.ERROR, message, details)
    }

    private fun log(level: LogLevel, message: String, details: LogDetails? = null) {
        if (level.priority >= logLevel.priority) {
            handle(level, message, details)
        }
    }

    enum class LogLevel(val value: String, val priority: Int) {
        DEBUG("debug", 1),
        INFO("info", 2),
        WARN("warn", 3),
        ERROR("error", 4),
        NONE("none", 5); // Special level to disable all logging
    }
}
