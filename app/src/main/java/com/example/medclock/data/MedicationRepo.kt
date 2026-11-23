package com.example.medclock.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** saves medication records to local storage*/
class MedicationRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getAll(): List<Medication> {
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Medication(
                id = o.getString("id"),
                name = o.getString("name"),
                dosage = o.getDouble("dosage"),
                dosageUnit = o.getString("dosageUnit")
            )
        }
    }

    /** Inserts a new record, or replaces the one with the same id (so US-05 edits can reuse it). */
    fun save(medication: Medication) {
        val updated = getAll().filter { it.id != medication.id } + medication
        val arr = JSONArray()
        updated.forEach {
            arr.put(JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("dosage", it.dosage)
                .put("dosageUnit", it.dosageUnit))
        }
        prefs.edit().putString(KEY, arr.toString()).commit()
    }

    companion object {
        private const val PREFS = "medclock_medications"
        private const val KEY = "medications"
    }
}