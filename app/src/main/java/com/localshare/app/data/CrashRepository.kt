package com.localshare.app.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class CrashRepository(private val context: Context) {

    companion object {
        private val KEY_CRASH_REPORTS = stringPreferencesKey("crash_reports")
        private const val MAX_REPORTS = 20
    }

    fun save(report: CrashReport) = runBlocking {
        val reports = loadAll().toMutableList()
        reports.add(0, report)
        if (reports.size > MAX_REPORTS) {
            reports.subList(MAX_REPORTS, reports.size).clear()
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_CRASH_REPORTS] = serialize(reports)
        }
    }

    fun loadAll(): List<CrashReport> = runBlocking {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_CRASH_REPORTS] ?: return@runBlocking emptyList()
        deserialize(json)
    }

    fun clear() = runBlocking {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CRASH_REPORTS)
        }
    }

    private fun serialize(reports: List<CrashReport>): String {
        val arr = JSONArray()
        for (r in reports) {
            val obj = JSONObject()
            obj.put("timestamp", r.timestamp)
            obj.put("exceptionClass", r.exceptionClass)
            obj.put("message", r.message)
            obj.put("stackTrace", r.stackTrace)
            obj.put("deviceModel", r.deviceModel)
            obj.put("androidVersion", r.androidVersion)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun deserialize(json: String): List<CrashReport> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                CrashReport(
                    timestamp = obj.optLong("timestamp", 0),
                    exceptionClass = obj.optString("exceptionClass", "Unknown"),
                    message = obj.optString("message", ""),
                    stackTrace = obj.optString("stackTrace", ""),
                    deviceModel = obj.optString("deviceModel", ""),
                    androidVersion = obj.optString("androidVersion", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
