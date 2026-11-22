package com.example.testbed.medclock

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.testbed.ui.theme.MedClockTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MahammadTasksUiTest {
    @get:Rule
    val compose = createComposeRule()

    private lateinit var context: Context
    private lateinit var store: MedClockStore
    private val storageName = "mahammad-ui-test-${System.nanoTime()}"

    @Before
    fun prepareSeparateTestStorage() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = MedClockStore(context, storageName)
        assertTrue(store.clearTestRecords())
    }

    @After
    fun removeTestRecords() {
        store.clearTestRecords()
    }

    private fun start(samples: Boolean = true) {
        if (samples) assertTrue(store.loadSampleRecords())
        compose.setContent { MedClockTheme { MedClockTestbed(store) } }
    }

    private fun openFirstMedication() {
        compose.onNodeWithTag("homeMedications").performClick()
        compose.onNodeWithTag("medication-sample-medication-a").performClick()
    }

    @Test
    fun anEmptyMedicationListDisplaysTheMessage() {
        start(samples = false)
        compose.onNodeWithTag("homeMedications").performClick()
        compose.onNodeWithText("No medications saved yet.").assertIsDisplayed()
    }

    @Test
    fun savingCorrectedDetailsRefreshesTheScreenAndPersistsTheRecord() {
        start()
        val untouched = store.loadMedications().last()
        openFirstMedication()
        compose.onNodeWithTag("editMedication").performScrollTo().performClick()
        compose.onNodeWithTag("editName").performTextReplacement("Corrected sample")
        compose.onNodeWithTag("dosageInput").performTextReplacement("2")
        compose.onNodeWithTag("dosageUnitInput").performTextReplacement("mg")
        compose.onNodeWithTag("saveMedication").performScrollTo().performClick()
        compose.onNodeWithTag("savedMedicationName").assertTextEquals("Corrected sample")
        compose.onNodeWithTag("savedMedicationDosage").assertTextEquals("2 mg")
        compose.runOnIdle {
            val reloaded = MedClockStore(context, storageName).loadMedications()
            assertEquals("Corrected sample", reloaded.first().name)
            assertEquals("08:00", reloaded.first().doseTime)
            assertEquals(30, reloaded.first().supply)
            assertEquals(untouched, reloaded.last())
        }
    }

    @Test
    fun cancellingSupplyEditsKeepsTheSavedQuantityAndUnit() {
        start()
        val original = store.loadMedications().first()
        openFirstMedication()
        compose.onNodeWithTag("openSupplyDraft").performScrollTo().performClick()
        compose.onNodeWithTag("supplyQuantityInput").performTextReplacement("999")
        compose.onNodeWithTag("supplyUnitInput").performTextReplacement("mL")
        compose.onNodeWithTag("cancelSupply").performScrollTo().performClick()
        compose.onNodeWithTag("savedSupply").assertTextEquals("30")
        compose.runOnIdle { assertEquals(original, MedClockStore(context, storageName).loadMedications().first()) }
    }

    @Test
    fun cancellingAnAppointmentReturnsToTheListWithoutSavingTheDraft() {
        start()
        val original = store.loadAppointments()
        compose.onNodeWithTag("homeAppointmentCancel").performClick()
        compose.onNodeWithTag("appointmentDoctorInput").performTextReplacement("Unsaved doctor")
        compose.onNodeWithTag("appointmentLocationInput").performScrollTo().performTextReplacement("Unsaved place")
        compose.onNodeWithTag("cancelAppointment").performScrollTo().performClick()
        compose.onNodeWithTag("appointmentList").assertIsDisplayed()
        compose.runOnIdle { assertEquals(original, MedClockStore(context, storageName).loadAppointments()) }
    }

    @Test
    fun theScheduleScreenLoadsSavedValuesAndCancellationDiscardsTheDraft() {
        start()
        val original = store.loadMedications().first()
        openFirstMedication()
        compose.onNodeWithTag("editSchedule").performScrollTo().performClick()
        compose.onNodeWithTag("scheduleTime").assertTextContains("08:00")
        compose.onNodeWithTag("dosageInput").assertTextContains("1")
        compose.onNodeWithTag("dosageUnitInput").assertTextContains("tablet")
        compose.onNodeWithTag("scheduleTime").performTextReplacement("20:00")
        compose.onNodeWithTag("cancelSchedule").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(original, store.loadMedications().first()) }
        compose.onNodeWithTag("editSchedule").performScrollTo().performClick()
        compose.onNodeWithTag("scheduleTime").assertTextContains("08:00")
    }

    @Test
    fun disablingRemindersChangesOnlyTheSelectedMedicationsSavedSetting() {
        start()
        val untouched = store.loadMedications().last()
        openFirstMedication()
        compose.onNodeWithTag("openReminders").performScrollTo().performClick()
        compose.onNodeWithTag("remindersSwitch").performClick().assertIsOff()
        compose.onNodeWithText("Disabled").assertIsDisplayed()
        compose.runOnIdle {
            val reloaded = MedClockStore(context, storageName).loadMedications()
            assertFalse(reloaded.first().remindersEnabled)
            assertEquals(untouched, reloaded.last())
        }
    }

    @Test
    fun editingReminderTimeOpensTheSavedValueAndRefreshesAfterSaving() {
        start()
        openFirstMedication()
        compose.onNodeWithTag("openReminders").performScrollTo().performClick()
        compose.onNodeWithTag("editReminderTime").performScrollTo().performClick()
        compose.onNodeWithTag("reminderTimeInput").assertTextContains("07:55")
        compose.onNodeWithTag("reminderTimeInput").performTextReplacement("07:30")
        compose.onNodeWithTag("saveReminderTime").performScrollTo().performClick()
        compose.onNodeWithTag("savedReminderTime").assertTextEquals("07:30")
        compose.runOnIdle { assertEquals("07:30", store.loadMedications().first().reminderTime) }
    }

    @Test
    fun theAppointmentListDisplaysTheEarlierRecordFirst() {
        start()
        compose.onNodeWithTag("homeAppointments").performClick()
        val earlier = compose.onNodeWithTag("appointment-sample-appointment-earlier").fetchSemanticsNode()
        val later = compose.onNodeWithTag("appointment-sample-appointment-later").fetchSemanticsNode()
        assertTrue(earlier.boundsInRoot.top < later.boundsInRoot.top)
        compose.onNodeWithText("Time: 09:15").assertIsDisplayed()
    }
}
