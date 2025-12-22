package com.thewintershadow.thoughtsmith.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized logging utility for the Thought Smith application.
 *
 * This object provides a unified logging interface that combines Android's built-in
 * logging with an in-memory log store for debugging and user support. All app components
 * should use this logger instead of direct Android Log calls.
 *
 * Features:
 * - Multiple log levels (DEBUG, INFO, WARNING, ERROR)
 * - In-memory log storage for viewing within the app
 * - Automatic log rotation to prevent memory issues
 * - Thread-safe operations
 * - Formatted log output for easy reading
 * - Integration with Android's system logging
 *
 * Log Storage:
 * - Stores up to 1000 recent log entries in memory
 * - Automatically removes oldest entries when limit exceeded
 * - Available for viewing in the app's Logs screen
 * - Can be exported as formatted text
 *
 * Usage:
 * ```kotlin
 * AppLogger.info("MainActivity", "App started successfully")
 * AppLogger.error("NetworkService", "Failed to connect", exception)
 * AppLogger.debug("ChatViewModel", "Processing user message")
 * ```
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
object AppLogger {
    /** Tag used for all Android Log entries */
    private const val TAG = "ThoughtSmith"

    /** Maximum number of log entries to keep in memory */
    private const val MAX_LOG_ENTRIES = 1000

    /** Thread-safe list of stored log entries */
    private val logEntries = mutableListOf<LogEntry>()

    /** Date formatter for consistent timestamp formatting */
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Represents a single log entry with metadata.
     *
     * @property timestamp Unix timestamp when the log was created
     * @property level The severity level of the log entry
     * @property tag The source component or class that created the log
     * @property message The log message content
     */
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
    ) {
        /**
         * Format the log entry as a human-readable string.
         *
         * @return Formatted string in the format: "[YYYY-MM-DD HH:MM:SS] [LEVEL] [TAG] MESSAGE"
         */
        fun formatted(): String {
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
            return "[$timeStr] [${level.name}] [$tag] $message"
        }
    }

    /**
     * Available log levels in order of severity.
     *
     * - DEBUG: Detailed information for debugging purposes
     * - INFO: General application flow and important events
     * - WARNING: Potentially problematic situations that don't break functionality
     * - ERROR: Error conditions and exceptions
     */
    enum class LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
    }

    /**
     * Log a debug message.
     *
     * Debug messages should contain detailed information useful during development
     * and troubleshooting. These may be filtered out in production builds.
     *
     * @param tag Source component (e.g., class name)
     * @param message Debug information
     */
    fun debug(
        tag: String,
        message: String,
    ) {
        log(LogLevel.DEBUG, tag, message)
        Log.d(TAG, "[$tag] $message")
    }

    /**
     * Log an informational message.
     *
     * Info messages document normal application flow and important events
     * like successful operations, state changes, and user actions.
     *
     * @param tag Source component (e.g., class name)
     * @param message Information about app state or operation
     */
    fun info(
        tag: String,
        message: String,
    ) {
        log(LogLevel.INFO, tag, message)
        Log.i(TAG, "[$tag] $message")
    }

    /**
     * Log a warning message.
     *
     * Warning messages indicate potentially problematic situations that don't
     * prevent the app from functioning but might need attention.
     *
     * @param tag Source component (e.g., class name)
     * @param message Warning description
     */
    fun warning(
        tag: String,
        message: String,
    ) {
        log(LogLevel.WARNING, tag, message)
        Log.w(TAG, "[$tag] $message")
    }

    /**
     * Log an error message with optional exception details.
     *
     * Error messages indicate problems that affect app functionality.
     * When a throwable is provided, its stack trace is included in the log.
     *
     * @param tag Source component (e.g., class name)
     * @param message Error description
     * @param throwable Optional exception that caused the error
     */
    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        val fullMessage =
            if (throwable != null) {
                "$message\n${throwable.stackTraceToString()}"
            } else {
                message
            }
        log(LogLevel.ERROR, tag, fullMessage)
        Log.e(TAG, "[$tag] $fullMessage", throwable)
    }

    /**
     * Internal method to store log entries in memory.
     *
     * This method is thread-safe and automatically manages the log entry limit
     * by removing oldest entries when the maximum is exceeded.
     *
     * @param level Log severity level
     * @param tag Source component
     * @param message Log message
     */
    private fun log(
        level: LogLevel,
        tag: String,
        message: String,
    ) {
        synchronized(logEntries) {
            val entry =
                LogEntry(
                    timestamp = System.currentTimeMillis(),
                    level = level,
                    tag = tag,
                    message = message,
                )
            logEntries.add(entry)

            // Keep only the most recent entries to prevent memory issues
            if (logEntries.size > MAX_LOG_ENTRIES) {
                logEntries.removeAt(0)
            }
        }
    }

    /**
     * Retrieve all stored log entries.
     *
     * Returns a thread-safe copy of all log entries currently in memory.
     * Used by the Logs screen to display recent app activity.
     *
     * @return List of LogEntry objects in chronological order
     */
    fun getLogs(): List<LogEntry> =
        synchronized(logEntries) {
            logEntries.toList()
        }

    /**
     * Clear all stored log entries.
     *
     * Removes all entries from memory and logs the clearing action.
     * Useful for starting fresh during debugging or to free memory.
     */
    fun clearLogs() {
        synchronized(logEntries) {
            logEntries.clear()
        }
        info("Logger", "Logs cleared")
    }

    /**
     * Get all log entries formatted as a single text string.
     *
     * This method formats all stored logs as a multi-line string suitable
     * for display in a text view or for exporting to a file.
     *
     * @return Formatted string containing all log entries, one per line
     */
    fun getLogsAsText(): String =
        synchronized(logEntries) {
            logEntries.joinToString("\n") { it.formatted() }
        }
}
