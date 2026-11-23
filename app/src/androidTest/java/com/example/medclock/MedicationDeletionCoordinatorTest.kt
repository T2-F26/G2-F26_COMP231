
package com.example.medclock

import com.example.medclock.data.MedicationDeletionCoordinator
import com.example.medclock.data.MedicationDeletionStatus
import org.junit.Assert.*
import org.junit.Test

class MedicationDeletionCoordinatorTest {

    @Test
    fun deletesMedicationAndRefreshesList() {

        // Fake medication storage for testing.
        val medications = mutableSetOf(
            "medication-1",
            "medication-2"
        )

        val cancelledReminders = mutableListOf<String>()
        var listRefreshed = false

        val coordinator = MedicationDeletionCoordinator(
            removeRecord = { id ->
                medications.remove(id)
            },
            cancelReminders = { id ->
                cancelledReminders.add(id)
                true
            },
            refreshList = {
                listRefreshed = true
            }
        )

        val result = coordinator.deleteConfirmed(
            "medication-1"
        )

        assertEquals(
            MedicationDeletionStatus.DELETED,
            result
        )

        assertFalse(medications.contains("medication-1"))
        assertTrue(medications.contains("medication-2"))
        assertEquals(
            listOf("medication-1"),
            cancelledReminders
        )
        assertTrue(listRefreshed)
    }

    @Test
    fun doesNotDeleteOtherMedications() {

        val medications = mutableSetOf(
            "medication-1",
            "medication-2"
        )

        var cancelCalled = false
        var refreshCalled = false

        val coordinator = MedicationDeletionCoordinator(
            removeRecord = { id ->
                medications.remove(id)
            },
            cancelReminders = {
                cancelCalled = true
                true
            },
            refreshList = {
                refreshCalled = true
            }
        )

        val result = coordinator.deleteConfirmed(
            "medication-3"
        )

        assertEquals(
            MedicationDeletionStatus.NOT_FOUND,
            result
        )

        assertEquals(2, medications.size)
        assertFalse(cancelCalled)
        assertFalse(refreshCalled)
    }

    @Test
    fun reportsReminderCancellationFailure() {

        var listRefreshed = false

        val coordinator = MedicationDeletionCoordinator(
            removeRecord = { true },
            cancelReminders = { false },
            refreshList = {
                listRefreshed = true
            }
        )

        val result = coordinator.deleteConfirmed(
            "medication-1"
        )

        assertEquals(
            MedicationDeletionStatus.REMINDER_CANCELLATION_FAILED,
            result
        )

        assertTrue(listRefreshed)
    }
}
