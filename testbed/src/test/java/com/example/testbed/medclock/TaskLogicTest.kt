package com.example.testbed.medclock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskLogicTest {
    private val medication = Medication(
        id = "saved-record", name = "Original name", dosage = "1", dosageUnit = "tablet",
        doseTime = "08:00", reminderTime = "07:55", remindersEnabled = false, supply = 30
    )

    @Test
    fun correctingDetailsPreservesScheduleSupplyAndReminderSettings() {
        val result = correctedMedication(medication, " Updated name ", " 2.5 ", " mg ")
        assertEquals("Updated name", result.name)
        assertEquals("2.5", result.dosage)
        assertEquals("mg", result.dosageUnit)
        assertEquals(medication.id, result.id)
        assertEquals(medication.doseTime, result.doseTime)
        assertEquals(medication.reminderTime, result.reminderTime)
        assertEquals(medication.remindersEnabled, result.remindersEnabled)
        assertEquals(medication.supply, result.supply)
        assertEquals("Original name", medication.name)
    }

    @Test
    fun dosageMustBeAPositiveFiniteNumber() {
        for (value in listOf("", "0", "-1", "abc", "NaN", "Infinity", "1e309")) {
            assertNotNull(medicationDetailsError("Sample", value, "mg"))
        }
        assertNull(medicationDetailsError("Sample", "0.5", "mg"))
    }

    @Test
    fun medicationNameAndUnitAreRequired() {
        assertNotNull(medicationDetailsError("  ", "1", "mg"))
        assertNotNull(medicationDetailsError("Sample", "1", "  "))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidCorrectionsCannotCreateASavedRecord() {
        correctedMedication(medication, "", "1", "mg")
    }

    @Test
    fun timeValidationAcceptsMidnightAndTheEndOfTheDay() {
        assertTrue(isValidTime("00:00"))
        assertTrue(isValidTime("23:59"))
    }

    @Test
    fun timeValidationRejectsInvalidOrAmbiguousTimes() {
        for (value in listOf("24:00", "12:60", "9:00", "09:00:30", "", "tomorrow")) {
            assertFalse(isValidTime(value))
        }
    }

    @Test
    fun appointmentsAreSortedByDateThenTimeWithoutChangingTheSourceList() {
        val later = Appointment("later", "Doctor B", "2026-12-01", "08:00", "Clinic")
        val afternoon = Appointment("afternoon", "Doctor A", "2026-11-30", "16:30", "Clinic")
        val morning = Appointment("morning", "Doctor C", "2026-11-30", "09:00", "Clinic")
        val records = listOf(later, afternoon, morning)
        assertEquals(listOf(morning, afternoon, later), appointmentsInDateOrder(records))
        assertEquals(listOf(later, afternoon, morning), records)
    }

    @Test
    fun anEmptyAppointmentListStaysEmpty() {
        assertTrue(appointmentsInDateOrder(emptyList()).isEmpty())
    }
}
