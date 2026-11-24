package com.example.medclock

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medclock.appointments.displayAppointmentDate
import com.example.medclock.data.MedicationName
import com.example.medclock.reminders.DraftValue
import com.example.medclock.reminders.ReminderTimeField
import com.example.medclock.screens.AddMedicationForm
import com.example.medclock.screens.AppointmentDetailsScreen
import com.example.medclock.screens.DeleteMedicationConfirmationDialog
import com.example.medclock.screens.DosageUnitDropdown
import com.example.medclock.screens.EditMedicationDetailsScreen
import com.example.medclock.screens.ReminderTimingCancelButton
import com.example.medclock.screens.RemainingSupplyText
import com.example.medclock.screens.SavedMedicationNamesList
import com.example.medclock.ui.theme.MedClockTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JosefinaScreensUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun savedMedicationNamesAreDisplayed() {
        composeTestRule.setContent {
            MedClockTheme {
                SavedMedicationNamesList(
                    medications = listOf(MedicationName("1", "Ibuprofen"), MedicationName("2", "Vitamin D")),
                    onMedicationClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Ibuprofen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vitamin D").assertIsDisplayed()
    }

    @Test
    fun emptyContentIsShownWhenNoMedicationsAreSaved() {
        composeTestRule.setContent {
            MedClockTheme {
                SavedMedicationNamesList(
                    medications = emptyList(),
                    onMedicationClick = {},
                    emptyContent = { Text("No medications saved yet.") }
                )
            }
        }

        composeTestRule.onNodeWithText("No medications saved yet.").assertIsDisplayed()
    }

    @Test
    fun addMedicationFormShowsNameDosageAndUnitFields() {
        composeTestRule.setContent {
            MedClockTheme {
                AddMedicationForm(
                    name = "",
                    onNameChange = {},
                    dosage = "",
                    onDosageChange = {},
                    dosageUnit = "",
                    onDosageUnitChange = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("medicationNameInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dosageInput").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dosageUnitInput").assertIsDisplayed()
    }

    @Test
    fun choosingADosageUnitFillsTheField() {
        composeTestRule.setContent {
            var unit by remember { mutableStateOf("") }
            MedClockTheme {
                DosageUnitDropdown(selectedUnit = unit, onUnitSelected = { unit = it })
            }
        }

        composeTestRule.onNodeWithTag("dosageUnitInput-open").performClick()
        composeTestRule.onNodeWithTag("dosageUnitInput-option-mL").performClick()

        composeTestRule.onNodeWithTag("dosageUnitInput").assertTextContains("mL")
    }

    @Test
    fun editScreenStartsWithCurrentValuesAndSavesChanges() {
        var saved: Triple<String, String, String>? = null
        composeTestRule.setContent {
            MedClockTheme {
                EditMedicationDetailsScreen(
                    currentName = "Ibuprofen",
                    currentDosage = "200",
                    currentDosageUnit = "mg",
                    onSave = { name, dosage, unit -> saved = Triple(name, dosage, unit) },
                    onCancel = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("medicationNameInput").assertTextContains("Ibuprofen")
        composeTestRule.onNodeWithTag("saveEditMedication").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("medicationNameInput").performTextReplacement("Advil")
        composeTestRule.onNodeWithTag("saveEditMedication").assertIsEnabled().performClick()

        assertEquals(Triple("Advil", "200", "mg"), saved)
    }

    @Test
    fun cancellingDeleteConfirmationDoesNotDelete() {
        var confirmed = false
        var cancelled = false
        composeTestRule.setContent {
            MedClockTheme {
                DeleteMedicationConfirmationDialog(
                    medicationName = "Ibuprofen",
                    onConfirm = { confirmed = true },
                    onCancel = { cancelled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Delete medication?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cancelDeleteMedication").performClick()

        assertTrue(cancelled)
        assertFalse(confirmed)
    }

    @Test
    fun confirmingDeleteCallsOnConfirm() {
        var confirmed = false
        composeTestRule.setContent {
            MedClockTheme {
                DeleteMedicationConfirmationDialog(
                    medicationName = "Ibuprofen",
                    onConfirm = { confirmed = true },
                    onCancel = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("confirmDeleteMedication").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun reminderTimePickerReturnsTheChosenTime() {
        var selected: String? = null
        composeTestRule.setContent {
            MedClockTheme {
                ReminderTimeField(currentTime = "08:30", onTimeSelected = { selected = it })
            }
        }

        composeTestRule.onNodeWithTag("reminderTimeField-change").performClick()
        composeTestRule.onNodeWithTag("confirmReminderTime").performClick()

        assertEquals("08:30", selected)
    }

    @Test
    fun remainingSupplyShowsQuantityOrNotRecorded() {
        composeTestRule.setContent {
            MedClockTheme {
                Column {
                    RemainingSupplyText(remainingQuantity = 24.0, unit = "tablets")
                    RemainingSupplyText(remainingQuantity = null, unit = "tablets")
                }
            }
        }

        composeTestRule.onNodeWithText("24 tablets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Not recorded").assertIsDisplayed()
    }

    @Test
    fun appointmentDetailsShowDoctorDateTimeAndLocation() {
        composeTestRule.setContent {
            MedClockTheme {
                AppointmentDetailsScreen(
                    doctorName = "Dr. Maria Lopez",
                    date = "2026-12-01",
                    time = "14:30",
                    location = "Downtown Family Clinic",
                    onBack = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("appointmentDoctor").assertTextEquals("Dr. Maria Lopez")
        composeTestRule.onNodeWithTag("appointmentDate").assertTextEquals(displayAppointmentDate("2026-12-01"))
        composeTestRule.onNodeWithTag("appointmentTime").assertIsDisplayed()
        composeTestRule.onNodeWithTag("appointmentLocation").assertTextEquals("Downtown Family Clinic")
    }

    @Test
    fun reminderTimingCancelKeepsThePreviousSetting() {
        var result: DraftValue<String>? = null
        composeTestRule.setContent {
            MedClockTheme {
                ReminderTimingCancelButton(
                    timing = DraftValue(saved = "1 day before", draft = "1 hour before"),
                    onCancel = { result = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("cancelReminderTiming").performClick()

        assertEquals(DraftValue(saved = "1 day before"), result)
    }
}
