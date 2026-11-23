
package com.example.medclock.data

// The possible results of a deletion request.
enum class MedicationDeletionStatus {
    DELETED,
    NOT_FOUND,
    REMINDER_CANCELLATION_FAILED
}

class MedicationDeletionCoordinator(
    private val removeRecord: (String) -> Boolean,
    private val cancelReminders: (String) -> Boolean,
    private val refreshList: () -> Unit
) {

    // Call this only AFTER the user confirms deletion.
    fun deleteConfirmed(
        medicationId: String
    ): MedicationDeletionStatus {

        require(medicationId.isNotBlank()) {
            "Medication ID cannot be empty"
        }

        // Step 1: Remove the medication record.
        val removed = removeRecord(medicationId)

        if (!removed) {
            return MedicationDeletionStatus.NOT_FOUND
        }

        // Step 2: Cancel future reminders.
        val cancelled = try {
            cancelReminders(medicationId)
        } catch (e: Exception) {
            false
        }

        // Step 3: Refresh the medication list.
        refreshList()

        return if (cancelled) {
            MedicationDeletionStatus.DELETED
        } else {
            MedicationDeletionStatus.REMINDER_CANCELLATION_FAILED
        }
    }
}
