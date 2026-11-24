package com.example.medclock.data

data class MedicationName(
    val id: String,
    val name: String
)

fun interface MedicationNameSource {
    fun loadNames(): List<MedicationName>
}

fun MedicationNameSource.savedMedicationNames(): List<MedicationName> =
    loadNames()
        .map { it.copy(name = it.name.trim()) }
        .filter { it.name.isNotEmpty() }
        .sortedWith(
            compareBy<MedicationName, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
                .thenBy { it.id }
        )
