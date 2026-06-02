package com.localshare.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * Represents a single access log entry.
 */
data class AccessLogEntry(
    val ip: String,
    val filename: String,
    val action: AccessAction,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

enum class AccessAction(val displayName: String) {
    BROWSE("Browse"),
    DOWNLOAD("Download"),
    STREAM("Stream")
}

/**
 * Thread-safe ring buffer for access logs.
 * Keeps only the most recent [maxSize] entries.
 */
class AccessLogBuffer(private val maxSize: Int = 200) {
    private val buffer = ConcurrentLinkedDeque<AccessLogEntry>()

    fun add(entry: AccessLogEntry) {
        buffer.addFirst(entry)
        while (buffer.size > maxSize) {
            buffer.removeLast()
        }
    }

    fun getAll(): List<AccessLogEntry> = buffer.toList()

    fun clear() = buffer.clear()

    val size: Int get() = buffer.size
}
