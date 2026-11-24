package com.example.medclock.supply

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

data class DoseConfirmation(
    val takenAt: LocalDateTime,
    val takenOnTime: Boolean
)


fun estimateRemainingSupply(
    currentSupply: Double,
    dosePerIntake: Double,
    confirmations: List<DoseConfirmation>,
    periodStart: LocalDateTime,
    periodEnd: LocalDateTime
): Double {
    require(currentSupply.isFinite() && currentSupply >= 0) { "Current supply cannot be negative." }
    require(dosePerIntake.isFinite() && dosePerIntake > 0) { "Dose per intake must be greater than zero." }
    require(!periodEnd.isBefore(periodStart)) { "The period cannot end before it starts." }

    val confirmedDoses = confirmations.count { confirmation ->
        confirmation.takenOnTime &&
                !confirmation.takenAt.isBefore(periodStart) &&
                !confirmation.takenAt.isAfter(periodEnd)
    }

    return (currentSupply - dosePerIntake * confirmedDoses).coerceAtLeast(0.0)
}

fun formatSupplyQuantity(value: Double): String =
    BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
