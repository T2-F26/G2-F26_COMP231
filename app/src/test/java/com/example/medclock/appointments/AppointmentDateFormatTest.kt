package com.example.medclock.appointments

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class AppointmentDateFormatTest {

    @Test
    fun displaysStoredDateWithWeekdayAndMonthName() {
        assertEquals("Tuesday, December 1, 2026", displayAppointmentDate("2026-12-01", Locale.US))
    }

    @Test
    fun displaysUnparsableDateUnchanged() {
        assertEquals("next week", displayAppointmentDate("next week", Locale.US))
    }
}
