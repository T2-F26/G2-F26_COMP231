
package com.example.medclock.data

object RemainingSupplyCalculator {

    // Calculate the number of full days of remaining supply.
    fun calculateRemainingDays(
        supplyQuantity: Double?,
        dailyConsumption: Double?
    ): Int? {

        // Return null when information is missing or invalid.
        if (supplyQuantity == null ||
            dailyConsumption == null
        ) {
            return null
        }

        if (!supplyQuantity.isFinite() ||
            !dailyConsumption.isFinite() ||
            supplyQuantity < 0 ||
            dailyConsumption <= 0
        ) {
            return null
        }

        val remainingDays = supplyQuantity / dailyConsumption

        if (!remainingDays.isFinite() ||
            remainingDays > Int.MAX_VALUE
        ) {
            return null
        }

        return remainingDays.toInt()
    }
}
