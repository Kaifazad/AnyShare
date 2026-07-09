package com.localshare.app.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CrashReport(
    val timestamp: Long,
    val exceptionClass: String,
    val message: String,
    val stackTrace: String,
    val deviceModel: String,
    val androidVersion: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(timestamp))

    val shortSummary: String
        get() = "$exceptionClass: ${message.take(100)}"
}
