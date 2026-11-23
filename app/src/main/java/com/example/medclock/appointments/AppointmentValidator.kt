package com.example.medclock.appointments

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

/** Required-field validation for the Add Appointment form. Notes are optional. */
object AppointmentValidator {

    /** Returns a map of field name -> error message. Empty map means the form is valid. */
    fun validate(doctor: String, location: String, date: String, time: String): Map<String, String> {
        val errors = linkedMapOf<String, String>()

        if (doctor.isBlank()) errors["doctor"] = "Doctor's name is required."
        if (location.isBlank()) errors["location"] = "Location is required."

        if (date.isBlank()) errors["date"] = "Date is required."
        else try { LocalDate.parse(date.trim()) } // yyyy-MM-dd
        catch (e: DateTimeParseException) { errors["date"] = "Enter a valid date (yyyy-MM-dd)." }

        if (time.isBlank()) errors["time"] = "Time is required."
        else try { LocalTime.parse(time.trim()) } // HH:mm
        catch (e: DateTimeParseException) { errors["time"] = "Enter a valid time (HH:mm)." }

        return errors
    }
}