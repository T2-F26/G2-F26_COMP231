package com.example.medclock.schedule

data class ValidationResult(val errors: List<String>) {
    val isValid: Boolean get() = errors.isEmpty()
}

/** Validates a daily medication schedule before it is saved. */
object ScheduleValidator {

    private val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$") // 24h "HH:mm"

    /**
     * @param doseTimes   dose times as "HH:mm"
     * @param doseQuantity amount per dose (e.g. 1.0 tablet)
     * @param doseUnit    unit chosen for the dose
     * @param supplyUnit  unit of the saved supply, or null if no supply recorded
     */
    fun validate(
        doseTimes: List<String>,
        doseQuantity: Double?,
        doseUnit: String,
        supplyUnit: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (doseTimes.isEmpty()) errors += "Enter at least one dose time."
        doseTimes.filterNot { TIME_REGEX.matches(it.trim()) }
            .forEach { errors += "\"$it\" is not a valid time (use HH:mm)." }
        if (doseTimes.map { it.trim() }.toSet().size != doseTimes.size) errors += "Dose times must not repeat."

        if (doseQuantity == null || doseQuantity <= 0.0) errors += "Dose quantity must be greater than zero."
        if (doseUnit.isBlank()) errors += "Select a dose unit."
        if (supplyUnit != null && doseUnit.isNotBlank() &&
            !doseUnit.trim().equals(supplyUnit.trim(), ignoreCase = true)
        ) errors += "Dose unit must match the supply unit ($supplyUnit)."

        return ValidationResult(errors)
    }
}