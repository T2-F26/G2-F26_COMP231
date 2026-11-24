package com.example.medclock.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DosageUnitsTest {

    @Test
    fun normalizesCaseAndWhitespace() {
        assertEquals("mL", normalizeDosageUnit(" ml "))
        assertEquals("IU", normalizeDosageUnit("iu"))
    }

    @Test
    fun normalizesSingularForms() {
        assertEquals("tablets", normalizeDosageUnit("tablet"))
        assertEquals("capsules", normalizeDosageUnit("Capsule"))
        assertEquals("drops", normalizeDosageUnit("drop"))
    }

    @Test
    fun returnsNullForUnsupportedUnits() {
        assertNull(normalizeDosageUnit("boxes"))
        assertNull(normalizeDosageUnit(""))
    }
}
