package com.example.medclock.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftValueTest {

    @Test
    fun newDraftHasNoChanges() {
        val timing = DraftValue(saved = "1 day before")

        assertEquals("1 day before", timing.draft)
        assertFalse(timing.hasChanges)
    }

    @Test
    fun updateChangesOnlyTheDraft() {
        val timing = DraftValue(saved = "1 day before").update("1 hour before")

        assertEquals("1 day before", timing.saved)
        assertEquals("1 hour before", timing.draft)
        assertTrue(timing.hasChanges)
    }

    @Test
    fun discardKeepsThePreviousSavedValue() {
        val timing = DraftValue(saved = "1 day before").update("1 hour before").discard()

        assertEquals("1 day before", timing.saved)
        assertEquals("1 day before", timing.draft)
        assertFalse(timing.hasChanges)
    }

    @Test
    fun commitSavesTheDraft() {
        val timing = DraftValue(saved = "1 day before").update("1 hour before").commit()

        assertEquals("1 hour before", timing.saved)
        assertFalse(timing.hasChanges)
    }
}
