package com.example.medclock.data

val DOSAGE_UNITS = listOf("mg", "mcg", "g", "mL", "tablets", "capsules", "drops", "IU")

private val SINGULAR_UNITS = mapOf(
    "tablet" to "tablets",
    "capsule" to "capsules",
    "drop" to "drops"
)


fun normalizeDosageUnit(unit: String): String? {
    val trimmed = unit.trim()
    return DOSAGE_UNITS.firstOrNull { it.equals(trimmed, ignoreCase = true) }
        ?: SINGULAR_UNITS[trimmed.lowercase()]
}
