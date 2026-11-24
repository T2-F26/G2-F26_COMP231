package com.example.medclock.appointments

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


fun displayAppointmentDate(value: String, locale: Locale = Locale.getDefault()): String =
    try {
        LocalDate.parse(value.trim())
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", locale))
    } catch (e: DateTimeParseException) {
        value
    }
