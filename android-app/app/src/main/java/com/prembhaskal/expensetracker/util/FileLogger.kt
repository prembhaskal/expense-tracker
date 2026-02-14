package com.prembhaskal.expensetracker.util

import android.content.Context
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val LOGCAT_TAG = "FileLogger"

/**
 * Composite logger: writes to both logcat (stdout/stderr) and a file via BufferedWriter.
 * File path: external files dir, or internal files dir if external is unavailable.
 * Access via Device File Explorer or adb pull. Path is printed at startup.
 */
object FileLogger {
    private var writer: BufferedWriter? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Synchronized
    fun init(context: Context?) {
        if (context == null) return
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val logsDir = File(dir, "logs").also { it.mkdirs() }
        val file = File(logsDir, "app.log")
        val path = file.absolutePath
        Log.i(LOGCAT_TAG, "Log file path: $path")
        println("FileLogger: Log file path: $path")
        try {
            writer = BufferedWriter(FileWriter(file, true))
            writer?.write("$path\n")
        } catch (_: Exception) { }
    }

    @Synchronized
    fun i(tag: String, message: String) {
        write("I", tag, message, null) { Log.i(tag, message) }
    }

    @Synchronized
    fun w(tag: String, message: String) {
        write("W", tag, message, null) { Log.w(tag, message) }
    }

    @Synchronized
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        write("E", tag, message, throwable) {
            if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
        }
    }

    private inline fun write(level: String, tag: String, message: String, throwable: Throwable?, logcat: () -> Unit) {
        logcat()
        val w = writer ?: return
        try {
            val timestamp = dateFormat.format(Date())
            val line = "$timestamp | $level | $tag | $message\n"
            w.write(line)
            if (throwable != null) {
                w.write("  ${throwable.stackTraceToString()}\n")
            }
        } catch (_: Exception) {
            // Silently ignore write failures
        }
    }
}
