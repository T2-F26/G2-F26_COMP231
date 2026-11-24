package com.example.medclock.supply

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDateTime

class SupplyEstimatorTest {

    private val periodStart = LocalDateTime.of(2026, 11, 1, 0, 0)
    private val periodEnd = LocalDateTime.of(2026, 11, 7, 23, 59)

    private fun onTime(day: Int) = DoseConfirmation(LocalDateTime.of(2026, 11, day, 8, 0), takenOnTime = true)

    @Test
    fun subtractsOneDoseForEachOnTimeConfirmation() {
        val remaining = estimateRemainingSupply(
            currentSupply = 30.0,
            dosePerIntake = 2.0,
            confirmations = listOf(onTime(2), onTime(3), onTime(4)),
            periodStart = periodStart,
            periodEnd = periodEnd
        )

        assertEquals(24.0, remaining, 0.0)
    }

    @Test
    fun ignoresLateConfirmations() {
        val late = DoseConfirmation(LocalDateTime.of(2026, 11, 3, 11, 0), takenOnTime = false)

        val remaining = estimateRemainingSupply(30.0, 1.0, listOf(onTime(2), late), periodStart, periodEnd)

        assertEquals(29.0, remaining, 0.0)
    }

    @Test
    fun ignoresConfirmationsOutsideThePeriod() {
        val before = DoseConfirmation(LocalDateTime.of(2026, 10, 31, 8, 0), takenOnTime = true)
        val after = DoseConfirmation(LocalDateTime.of(2026, 11, 8, 8, 0), takenOnTime = true)

        val remaining = estimateRemainingSupply(30.0, 1.0, listOf(before, onTime(5), after), periodStart, periodEnd)

        assertEquals(29.0, remaining, 0.0)
    }

    @Test
    fun countsConfirmationsExactlyAtThePeriodBoundaries() {
        val atStart = DoseConfirmation(periodStart, takenOnTime = true)
        val atEnd = DoseConfirmation(periodEnd, takenOnTime = true)

        val remaining = estimateRemainingSupply(10.0, 1.0, listOf(atStart, atEnd), periodStart, periodEnd)

        assertEquals(8.0, remaining, 0.0)
    }

    @Test
    fun returnsCurrentSupplyWhenNothingWasConfirmed() {
        val remaining = estimateRemainingSupply(12.5, 1.0, emptyList(), periodStart, periodEnd)

        assertEquals(12.5, remaining, 0.0)
    }

    @Test
    fun neverGoesBelowZero() {
        val confirmations = (1..7).map { onTime(it) }

        val remaining = estimateRemainingSupply(3.0, 1.0, confirmations, periodStart, periodEnd)

        assertEquals(0.0, remaining, 0.0)
    }

    @Test
    fun rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            estimateRemainingSupply(-1.0, 1.0, emptyList(), periodStart, periodEnd)
        }
        assertThrows(IllegalArgumentException::class.java) {
            estimateRemainingSupply(10.0, 0.0, emptyList(), periodStart, periodEnd)
        }
        assertThrows(IllegalArgumentException::class.java) {
            estimateRemainingSupply(10.0, 1.0, emptyList(), periodEnd, periodStart)
        }
    }

    @Test
    fun formatsQuantitiesWithoutTrailingZeros() {
        assertEquals("24", formatSupplyQuantity(24.0))
        assertEquals("2.5", formatSupplyQuantity(2.5))
        assertEquals("2.33", formatSupplyQuantity(2.3333))
        assertEquals("0", formatSupplyQuantity(0.0))
        assertEquals("100", formatSupplyQuantity(100.0))
    }
}
