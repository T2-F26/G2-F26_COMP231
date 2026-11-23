
package com.example.medclock.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.util.UUID

data class SavedMedication(
    val id: String,
    val name: String,
    val dosage: String,
    val supplyQuantity: Double? = null,
    val supplyUnit: String = "tablets",
    val stockUpdatedAtMillis: Long? = null,
    val doseTimes: List<String> = emptyList()
)

class MedClockDemoMedicationStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "medclock_demo_medications",
            Context.MODE_PRIVATE
        )

    private val storageKey = "medications"

    // Retrieve all saved medications.
    fun getMedications(): List<SavedMedication> {

        val json =
            preferences.getString(storageKey, "[]") ?: "[]"

        val array = JSONArray(json)

        return (0 until array.length()).map { index ->

            val item = array.getJSONObject(index)

            SavedMedication(
                id = item.getString("id"),
                name = item.getString("name"),
                dosage = item.getString("dosage"),
                supplyQuantity =
                    if (item.isNull("supplyQuantity")) {
                        null
                    } else {
                        item.getDouble("supplyQuantity")
                    },
                supplyUnit =
                    normalizeUnit(
                        item.optString("supplyUnit", "tablets")
                    ) ?: "tablets",
                stockUpdatedAtMillis =
                    if (item.isNull("stockUpdatedAtMillis")) {
                        null
                    } else {
                        item.getLong("stockUpdatedAtMillis")
                    },
                doseTimes =
                    item.optJSONArray("doseTimes")?.let { times ->
                        (0 until times.length()).map {
                            times.getString(it)
                        }
                    } ?: emptyList()
            )
        }
    }

    // Add a new medication.
    fun addMedication(
        name: String,
        dosage: String,
        supplyUnit: String = "tablets"
    ): SavedMedication {

        require(name.isNotBlank())
        require(dosage.isNotBlank())

        val unit = requireNotNull(normalizeUnit(supplyUnit))

        val medication = SavedMedication(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            dosage = dosage.trim(),
            supplyUnit = unit
        )

        check(saveAll(getMedications() + medication))

        return medication
    }

    // Save or update medication supply.
    fun updateSupply(
        medicationId: String,
        quantityInput: String,
        unit: String
    ): Boolean {

        val quantity =
            quantityInput.trim().toDoubleOrNull()
                ?: return false

        if (!quantity.isFinite() || quantity < 0) {
            return false
        }

        val medications = getMedications()

        val selected =
            medications.firstOrNull {
                it.id == medicationId
            } ?: return false

        if (normalizeUnit(unit) != selected.supplyUnit) {
            return false
        }

        val updated = medications.map { medication ->

            if (medication.id == medicationId) {
                medication.copy(
                    supplyQuantity = quantity,
                    stockUpdatedAtMillis =
                        System.currentTimeMillis()
                )
            } else {
                medication
            }
        }

        return saveAll(updated)
    }

    // Save changes to daily medication times.
    fun updateSchedule(
        medicationId: String,
        newTimes: List<String>
    ): Boolean {

        val times = newTimes.map { it.trim() }

        if (times.isEmpty()) {
            return false
        }

        if (times.distinct().size != times.size) {
            return false
        }

        if (times.any { !isValidTime(it) }) {
            return false
        }

        val medications = getMedications()

        if (medications.none { it.id == medicationId }) {
            return false
        }

        val updated = medications.map { medication ->

            if (medication.id == medicationId) {
                medication.copy(doseTimes = times)
            } else {
                medication
            }
        }

        return saveAll(updated)
    }

    // Permanently delete a saved medication record.
    fun deleteMedication(medicationId: String): Boolean {

        if (medicationId.isBlank()) {
            return false
        }

        val medications = getMedications()

        if (medications.none { it.id == medicationId }) {
            return false
        }

        val updated = medications.filterNot {
            it.id == medicationId
        }

        return saveAll(updated)
    }

    // Save the medication list to local storage.
    private fun saveAll(
        medications: List<SavedMedication>
    ): Boolean {

        val array = JSONArray()

        medications.forEach { medication ->

            val item = JSONObject().apply {

                put("id", medication.id)
                put("name", medication.name)
                put("dosage", medication.dosage)

                put(
                    "supplyQuantity",
                    medication.supplyQuantity ?: JSONObject.NULL
                )

                put("supplyUnit", medication.supplyUnit)

                put(
                    "stockUpdatedAtMillis",
                    medication.stockUpdatedAtMillis
                        ?: JSONObject.NULL
                )

                put(
                    "doseTimes",
                    JSONArray().apply {
                        medication.doseTimes.forEach { time ->
                            put(time)
                        }
                    }
                )
            }

            array.put(item)
        }

        return preferences.edit()
            .putString(storageKey, array.toString())
            .commit()
    }

    // Keep supply units consistent.
    private fun normalizeUnit(value: String): String? {

        return when (value.trim().lowercase()) {
            "tablet", "tablets" -> "tablets"
            "capsule", "capsules" -> "capsules"
            "ml" -> "mL"
            else -> null
        }
    }

    // Check whether a time uses valid 24-hour HH:mm format.
    private fun isValidTime(value: String): Boolean {

        val pattern =
            Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")

        return pattern.matches(value) &&
                runCatching {
                    LocalTime.parse(value)
                }.isSuccess
    }
}
