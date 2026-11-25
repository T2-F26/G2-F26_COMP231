
package com.example.medclock

import com.example.medclock.schedule.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class UpcomingDoseFinderTest {

    @Test
    fun findsNextDoseTimes() {

        // Fake saved data used only for testing.
        val source = object : MedicationScheduleSource {
            override fun loadSchedules() = listOf(
                SavedDoseSchedule(
                    medicationId = "med-1",
                    medicationName = "Paracetamol",
                    doseTimes = listOf(
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                    )
                )
            )
        }

        val finder = UpcomingDoseFinder(source)

        val currentTime = LocalDateTime.of(
            2026, 11, 22, 10, 0
        )

        val result = finder.findUpcomingDoses(currentTime)

        assertEquals(2, result.size)

        // First upcoming dose: today, 8 PM.
        assertEquals(
            LocalDateTime.of(2026, 11, 22, 20, 0),
            result[0].scheduledAt
        )

        // Second upcoming dose: tomorrow, 8 AM.
        assertEquals(
            LocalDateTime.of(2026, 11, 23, 8, 0),
            result[1].scheduledAt
        )
    }

    @Test
    fun disabledRemindersAreSkipped() {

        val source = object : MedicationScheduleSource {
            override fun loadSchedules() = listOf(
                SavedDoseSchedule(
                    medicationId = "med-2",
                    medicationName = "Vitamin D",
                    doseTimes = listOf(LocalTime.of(12, 0)),
                    remindersEnabled = false
                )
            )
        }

        val finder = UpcomingDoseFinder(source)

        val result = finder.findUpcomingDoses(
            LocalDateTime.of(2026, 11, 22, 10, 0)
        )

        assertTrue(result.isEmpty())
    }
}
