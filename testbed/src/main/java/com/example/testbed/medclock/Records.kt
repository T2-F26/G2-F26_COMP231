package com.example.testbed.medclock

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val dosageUnit: String,
    val doseTime: String = "",
    val reminderTime: String = "",
    val remindersEnabled: Boolean = true,
    val supply: Int? = null
)

data class Appointment(
    val id: String,
    val doctorName: String,
    val date: String,
    val time: String,
    val location: String,
    val notes: String = ""
)

fun medicationDetailsError(name: String, dosage: String, dosageUnit: String): String? {
    if (name.isBlank()) return "Enter a medication name."
    val amount = dosage.trim().toDoubleOrNull()
    if (amount == null || !amount.isFinite() || amount <= 0) {
        return "Enter a dosage greater than zero."
    }
    if (dosageUnit.isBlank()) return "Enter a dosage unit."
    return null
}

fun correctedMedication(
    medication: Medication,
    name: String,
    dosage: String,
    dosageUnit: String
): Medication {
    val error = medicationDetailsError(name, dosage, dosageUnit)
    require(error == null) { error ?: "Invalid medication details." }
    return medication.copy(
        name = name.trim(),
        dosage = dosage.trim(),
        dosageUnit = dosageUnit.trim()
    )
}

fun isValidTime(value: String): Boolean {
    if (!Regex("[0-9]{2}:[0-9]{2}").matches(value)) return false
    return try {
        LocalTime.parse(value)
        true
    } catch (error: java.time.format.DateTimeParseException) {
        false
    }
}

fun appointmentsInDateOrder(appointments: List<Appointment>): List<Appointment> {
    return appointments.sortedWith(
        compareBy<Appointment> {
            LocalDateTime.of(LocalDate.parse(it.date), LocalTime.parse(it.time))
        }.thenBy { it.id }
    )
}
