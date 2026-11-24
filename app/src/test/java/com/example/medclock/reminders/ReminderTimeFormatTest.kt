package com.example.medclock.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime
import java.util.Locale

class ReminderTimeFormatTest {

    @Test
    fun parsesStoredTimes() {
        assertEquals(LocalTime.of(8, 5), parseClockTime("08:05"))
        assertEquals(LocalTime.of(20, 30), parseClockTime(" 20:30 "))
    }

    @Test
    fun rejectsTimesNotInHourMinuteFormat() {
        assertNull(parseClockTime("8:05"))
        assertNull(parseClockTime("24:00"))
        assertNull(parseClockTime("12:60"))
        assertNull(parseClockTime("noon"))
        assertNull(parseClockTime(""))
    }

    @Test
    fun formatsHourAndMinuteWithLeadingZeros() {
        assertEquals("08:05", formatClockTime(8, 5))
        assertEquals("00:00", formatClockTime(0, 0))
        assertEquals("23:59", formatClockTime(23, 59))
    }

    @Test
    fun displaysTimeInTwelveOrTwentyFourHourStyle() {
        assertEquals("20:30", displayClockTime("20:30", is24Hour = true, locale = Locale.US))
        assertEquals("8:30 PM", displayClockTime("20:30", is24Hour = false, locale = Locale.US))
        assertEquals("12:00 AM", displayClockTime("00:00", is24Hour = false, locale = Locale.US))
    }

    @Test
    fun displaysUnparsableTimeUnchanged() {
        assertEquals("soon", displayClockTime("soon", is24Hour = true))
    }
}
