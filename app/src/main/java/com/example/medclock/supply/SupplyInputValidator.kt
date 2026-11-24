package com.example.medclock.supply

import com.example.medclock.data.DOSAGE_UNITS
import com.example.medclock.data.normalizeDosageUnit

object SupplyInputValidator {

    const val QUANTITY = "quantity"
    const val UNIT = "unit"


    fun validate(
        quantityText: String,
        unit: String,
        expectedUnit: String? = null
    ): Map<String, String> {
        val errors = linkedMapOf<String, String>()

        val quantity = quantityText.trim().toDoubleOrNull()
        if (quantityText.isBlank()) {
            errors[QUANTITY] = "Quantity is required."
        } else if (quantity == null || !quantity.isFinite()) {
            errors[QUANTITY] = "Enter a number, for example 30."
        } else if (quantity < 0) {
            errors[QUANTITY] = "Quantity cannot be negative."
        }

        val normalizedUnit = normalizeDosageUnit(unit)
        if (unit.isBlank()) {
            errors[UNIT] = "Dosage unit is required."
        } else if (normalizedUnit == null) {
            errors[UNIT] = "Choose one of: ${DOSAGE_UNITS.joinToString()}."
        } else if (expectedUnit != null && normalizedUnit != normalizeDosageUnit(expectedUnit)) {
            errors[UNIT] = "Unit must match the medication's dosage unit ($expectedUnit)."
        }

        return errors
    }
}
