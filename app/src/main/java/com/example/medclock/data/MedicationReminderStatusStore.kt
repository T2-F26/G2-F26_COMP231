
package com.example.medclock.data

import android.content.Context

class MedicationReminderStatusStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "medclock_reminder_settings",
            Context.MODE_PRIVATE
        )

    private fun keyFor(medicationId: String): String {
        return "reminder_enabled_$medicationId"
    }

    // Save whether reminders are enabled for one medication.
    fun saveReminderEnabled(
        medicationId: String,
        enabled: Boolean
    ): Boolean {

        if (medicationId.isBlank()) {
            return false
        }

        return preferences.edit()
            .putBoolean(keyFor(medicationId), enabled)
            .commit()
    }

    // Retrieve the saved reminder status.
    // Returns null when no setting has been saved yet.
    fun getReminderEnabled(
        medicationId: String
    ): Boolean? {

        if (medicationId.isBlank()) {
            return null
        }

        val key = keyFor(medicationId)

        if (!preferences.contains(key)) {
            return null
        }

        return preferences.getBoolean(key, true)
    }

    // Remove the reminder setting when a medication is deleted.
    fun deleteReminderStatus(
        medicationId: String
    ): Boolean {

        if (medicationId.isBlank()) {
            return false
        }

        return preferences.edit()
            .remove(keyFor(medicationId))
            .commit()
    }
}
