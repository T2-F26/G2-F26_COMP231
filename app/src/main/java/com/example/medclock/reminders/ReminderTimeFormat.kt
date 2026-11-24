package com.example.medclock.reminders

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

private val STORED_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
    .withResolverStyle(ResolverStyle.STRICT)

fun parseClockTime(value: String): LocalTime? =
    try {
        LocalTime.parse(value.trim(), STORED_TIME_FORMAT)
    } catch (e: DateTimeParseException) {
        null
    }

fun formatClockTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(STORED_TIME_FORMAT)

fun displayClockTime(value: String, is24Hour: Boolean, locale: Locale = Locale.getDefault()): String {
    val time = parseClockTime(value) ?: return value
    val pattern = if (is24Hour) "HH:mm" else "h:mm a"
    return time.format(DateTimeFormatter.ofPattern(pattern, locale))
}
