package com.example.testbed.medclock

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate

class MedClockStore(
    context: Context,
    storageName: String = "mahammad_medclock_testbed"
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        storageName,
        Context.MODE_PRIVATE
    )

    fun loadMedications(): List<Medication> {
        val records = readArray("medications")
        val medications = mutableListOf<Medication>()
        for (index in 0 until records.length()) {
            val record = records.optJSONObject(index) ?: continue
            if (record.optString("id").isBlank()) continue
            medications.add(
                Medication(
                    id = record.optString("id"),
                    name = record.optString("name"),
                    dosage = record.optString("dosage"),
                    dosageUnit = record.optString("dosageUnit"),
                    doseTime = record.optString("doseTime"),
                    reminderTime = record.optString("reminderTime"),
                    remindersEnabled = record.optBoolean("remindersEnabled", true),
                    supply = if (record.isNull("supply")) null else record.optInt("supply")
                )
            )
        }
        return medications
    }

    fun updateMedication(updated: Medication): Boolean {
        val medications = loadMedications().toMutableList()
        val index = medications.indexOfFirst { it.id == updated.id }
        if (index == -1) return false
        medications[index] = updated
        return preferences.edit()
            .putString("medications", medicationArray(medications).toString())
            .commit()
    }

    fun loadAppointments(): List<Appointment> {
        val records = readArray("appointments")
        val appointments = mutableListOf<Appointment>()
        for (index in 0 until records.length()) {
            val record = records.optJSONObject(index) ?: continue
            val id = record.optString("id")
            val date = record.optString("date")
            val time = record.optString("time")
            if (id.isBlank() || !isValidTime(time)) continue
            try {
                LocalDate.parse(date)
            } catch (error: java.time.format.DateTimeParseException) {
                continue
            }
            appointments.add(
                Appointment(
                    id = id,
                    doctorName = record.optString("doctorName"),
                    date = date,
                    time = time,
                    location = record.optString("location"),
                    notes = record.optString("notes")
                )
            )
        }
        return appointmentsInDateOrder(appointments)
    }

    fun loadSampleRecords(): Boolean {
        val medications = listOf(
            Medication(
                id = "sample-medication-a",
                name = "Sample medication A",
                dosage = "1",
                dosageUnit = "tablet",
                doseTime = "08:00",
                reminderTime = "07:55",
                supply = 30
            ),
            Medication(
                id = "sample-medication-b",
                name = "Sample medication B",
                dosage = "1",
                dosageUnit = "capsule"
            )
        )
        val today = LocalDate.now()
        val appointments = JSONArray()
        appointments.put(
            appointmentObject(
                Appointment(
                    id = "sample-appointment-later",
                    doctorName = "Example doctor B",
                    date = today.plusDays(3).toString(),
                    time = "14:30",
                    location = "Example clinic B"
                )
            )
        )
        appointments.put(
            appointmentObject(
                Appointment(
                    id = "sample-appointment-earlier",
                    doctorName = "Example doctor A",
                    date = today.plusDays(1).toString(),
                    time = "09:15",
                    location = "Example clinic A"
                )
            )
        )
        return preferences.edit()
            .putString("medications", medicationArray(medications).toString())
            .putString("appointments", appointments.toString())
            .commit()
    }

    fun clearTestRecords(): Boolean {
        return preferences.edit().clear().commit()
    }

    private fun readArray(key: String): JSONArray {
        return try {
            JSONArray(preferences.getString(key, "[]") ?: "[]")
        } catch (error: JSONException) {
            JSONArray()
        }
    }

    private fun medicationArray(medications: List<Medication>): JSONArray {
        val records = JSONArray()
        for (medication in medications) {
            records.put(
                JSONObject()
                    .put("id", medication.id)
                    .put("name", medication.name)
                    .put("dosage", medication.dosage)
                    .put("dosageUnit", medication.dosageUnit)
                    .put("doseTime", medication.doseTime)
                    .put("reminderTime", medication.reminderTime)
                    .put("remindersEnabled", medication.remindersEnabled)
                    .put("supply", medication.supply ?: JSONObject.NULL)
            )
        }
        return records
    }

    private fun appointmentObject(appointment: Appointment): JSONObject {
        return JSONObject()
            .put("id", appointment.id)
            .put("doctorName", appointment.doctorName)
            .put("date", appointment.date)
            .put("time", appointment.time)
            .put("location", appointment.location)
            .put("notes", appointment.notes)
    }
}
