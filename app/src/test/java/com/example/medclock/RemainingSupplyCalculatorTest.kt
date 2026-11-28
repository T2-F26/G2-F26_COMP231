
package com.example.medclock

import com.example.medclock.data.RemainingSupplyCalculator
import org.junit.Assert.*
import org.junit.Test

class RemainingSupplyCalculatorTest {

    @Test
    fun calculatesRemainingDays() {
        assertEquals(
            15,
            RemainingSupplyCalculator
                .calculateRemainingDays(30.0, 2.0)
        )
    }

    @Test
    fun roundsDownToFullDays() {
        assertEquals(
            7,
            RemainingSupplyCalculator
                .calculateRemainingDays(15.0, 2.0)
        )
    }

    @Test
    fun handlesZeroSupply() {
        assertEquals(
            0,
            RemainingSupplyCalculator
                .calculateRemainingDays(0.0, 2.0)
        )
    }

    @Test
    fun handlesMissingSupply() {
        assertNull(
            RemainingSupplyCalculator
                .calculateRemainingDays(null, 2.0)
        )
    }

    @Test
    fun handlesMissingDailyConsumption() {
        assertNull(
            RemainingSupplyCalculator
                .calculateRemainingDays(30.0, null)
        )
    }

    @Test
    fun handlesZeroDailyConsumption() {
        assertNull(
            RemainingSupplyCalculator
                .calculateRemainingDays(30.0, 0.0)
        )
    }

    @Test
    fun handlesNegativeSupply() {
        assertNull(
            RemainingSupplyCalculator
                .calculateRemainingDays(-10.0, 2.0)
        )
    }

    @Test
    fun handlesNegativeDailyConsumption() {
        assertNull(
            RemainingSupplyCalculator
                .calculateRemainingDays(30.0, -2.0)
        )
    }
}
