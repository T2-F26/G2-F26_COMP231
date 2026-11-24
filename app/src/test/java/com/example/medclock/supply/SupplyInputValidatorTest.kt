package com.example.medclock.supply

import com.example.medclock.supply.SupplyInputValidator.QUANTITY
import com.example.medclock.supply.SupplyInputValidator.UNIT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplyInputValidatorTest {

    @Test
    fun acceptsValidQuantityAndUnit() {
        assertTrue(SupplyInputValidator.validate("30", "tablets").isEmpty())
        assertTrue(SupplyInputValidator.validate(" 12.5 ", "mL").isEmpty())
        assertTrue(SupplyInputValidator.validate("0", "capsules").isEmpty())
    }

    @Test
    fun requiresQuantity() {
        val errors = SupplyInputValidator.validate("  ", "tablets")

        assertEquals(setOf(QUANTITY), errors.keys)
        assertEquals("Quantity is required.", errors[QUANTITY])
    }

    @Test
    fun rejectsQuantityThatIsNotANumber() {
        assertEquals(setOf(QUANTITY), SupplyInputValidator.validate("thirty", "tablets").keys)
        assertEquals(setOf(QUANTITY), SupplyInputValidator.validate("NaN", "tablets").keys)
        assertEquals(setOf(QUANTITY), SupplyInputValidator.validate("Infinity", "tablets").keys)
    }

    @Test
    fun rejectsNegativeQuantity() {
        val errors = SupplyInputValidator.validate("-5", "tablets")

        assertEquals("Quantity cannot be negative.", errors[QUANTITY])
    }

    @Test
    fun requiresUnit() {
        val errors = SupplyInputValidator.validate("30", "")

        assertEquals(setOf(UNIT), errors.keys)
        assertEquals("Dosage unit is required.", errors[UNIT])
    }

    @Test
    fun rejectsUnknownUnit() {
        assertEquals(setOf(UNIT), SupplyInputValidator.validate("30", "boxes").keys)
    }

    @Test
    fun acceptsUnitsIgnoringCaseAndSingularForm() {
        assertTrue(SupplyInputValidator.validate("30", "ML").isEmpty())
        assertTrue(SupplyInputValidator.validate("30", "Tablet").isEmpty())
    }

    @Test
    fun unitMustMatchExpectedUnitWhenGiven() {
        val mismatch = SupplyInputValidator.validate("30", "mg", expectedUnit = "tablets")
        val match = SupplyInputValidator.validate("30", "tablet", expectedUnit = "tablets")

        assertEquals(setOf(UNIT), mismatch.keys)
        assertTrue(match.isEmpty())
    }

    @Test
    fun reportsBothFieldsAtOnce() {
        val errors = SupplyInputValidator.validate("", "")

        assertEquals(setOf(QUANTITY, UNIT), errors.keys)
    }
}
