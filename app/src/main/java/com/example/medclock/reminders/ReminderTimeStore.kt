package com.example.medclock.reminders

import android.content.Context

/** Persists each medication's reminder times ("HH:mm") in local storage. */
class ReminderTimeStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("medclock_reminder_times", Context.MODE_PRIVATE)

    fun get(medId: String): List<String> =
        prefs.getString(medId, "")!!.split(",").filter { it.isNotBlank() }

    fun save(medId: String, times: List<String>) {
        prefs.edit().putString(medId, times.joinToString(",")).commit()
    }
}