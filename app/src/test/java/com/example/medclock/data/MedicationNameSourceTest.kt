package com.example.medclock.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationNameSourceTest {

    @Test
    fun returnsNamesSortedAlphabeticallyIgnoringCase() {
        val source = MedicationNameSource {
            listOf(
                MedicationName("1", "vitamin D"),
                MedicationName("2", "Ibuprofen"),
                MedicationName("3", "Aspirin")
            )
        }

        assertEquals(
            listOf("Aspirin", "Ibuprofen", "vitamin D"),
            source.savedMedicationNames().map { it.name }
        )
    }

    @Test
    fun trimsNamesAndDropsBlankOnes() {
        val source = MedicationNameSource {
            listOf(
                MedicationName("1", "  Metformin "),
                MedicationName("2", "   "),
                MedicationName("3", "")
            )
        }

        assertEquals(listOf(MedicationName("1", "Metformin")), source.savedMedicationNames())
    }

    @Test
    fun keepsMedicationsWithTheSameName() {
        val source = MedicationNameSource {
            listOf(MedicationName("b", "Ibuprofen"), MedicationName("a", "Ibuprofen"))
        }

        assertEquals(listOf("a", "b"), source.savedMedicationNames().map { it.id })
    }

    @Test
    fun returnsEmptyListWhenNothingIsSaved() {
        assertTrue(MedicationNameSource { emptyList() }.savedMedicationNames().isEmpty())
    }
}
