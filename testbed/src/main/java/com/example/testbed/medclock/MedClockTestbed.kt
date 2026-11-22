package com.example.testbed.medclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun MedClockTestbed(suppliedStore: MedClockStore? = null) {
    val context = LocalContext.current
    val store = remember(suppliedStore) { suppliedStore ?: MedClockStore(context) }
    var medications by remember(store) { mutableStateOf(store.loadMedications()) }
    var appointments by remember(store) { mutableStateOf(store.loadAppointments()) }
    var page by rememberSaveable { mutableStateOf("home") }
    var selectedId by rememberSaveable { mutableStateOf("") }
    var notice by rememberSaveable { mutableStateOf("") }
    var dataAction by rememberSaveable { mutableStateOf("") }
    val selectedMedication = medications.firstOrNull { it.id == selectedId }

    fun saveMedication(updated: Medication, nextPage: String): String? {
        if (!store.updateMedication(updated)) return "The record could not be saved. Please try again."
        medications = store.loadMedications()
        page = nextPage
        return null
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (page) {
                "home" -> Column(
                    modifier = Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("MedClock Testbed", style = MaterialTheme.typography.headlineMedium)
                    Text("Mahammad Mammadov | Iteration 1")
                    Text("Load sample records, then open a screen to check your tasks.")
                    Button(
                        onClick = { page = "medications" },
                        modifier = Modifier.fillMaxWidth().testTag("homeMedications")
                    ) { Text("Medications") }
                    Button(
                        onClick = { page = "appointments" },
                        modifier = Modifier.fillMaxWidth().testTag("homeAppointments")
                    ) { Text("Appointments") }
                    OutlinedButton(
                        onClick = { page = "appointmentDraft" },
                        modifier = Modifier.fillMaxWidth().testTag("homeAppointmentCancel")
                    ) { Text("Test appointment cancellation") }
                    OutlinedButton(
                        onClick = { dataAction = "load" },
                        modifier = Modifier.fillMaxWidth().testTag("loadSampleRecords")
                    ) { Text("Load sample records") }
                    TextButton(
                        onClick = { dataAction = "clear" },
                        modifier = Modifier.fillMaxWidth().testTag("clearTestRecords")
                    ) { Text("Clear test records") }
                    if (notice.isNotBlank()) Text(notice)
                }

                "medications" -> MedicationListScreen(
                    medications = medications,
                    onOpen = { selectedId = it; page = "details" },
                    onBack = { page = "home" }
                )

                "appointments" -> AppointmentListScreen(appointments, onBack = { page = "home" })

                "appointmentDraft" -> AppointmentDraftScreen(onCancel = { page = "appointments" })

                else -> if (selectedMedication == null) {
                    TaskPage("Medication unavailable", onBack = { page = "medications" }) {
                        Text("This record is no longer available. Return to the medication list.")
                    }
                } else {
                    when (page) {
                        "details" -> MedicationDetailsScreen(
                            medication = selectedMedication,
                            onEdit = { page = "editMedication" },
                            onSchedule = { page = "schedule" },
                            onReminders = { page = "reminders" },
                            onSupply = { page = "supplyDraft" },
                            onBack = { page = "medications" }
                        )

                        "editMedication" -> MedicationEditScreen(
                            medication = selectedMedication,
                            onSave = { name, dosage, unit ->
                                saveMedication(correctedMedication(selectedMedication, name, dosage, unit), "details")
                            },
                            onCancel = { page = "details" }
                        )

                        "schedule" -> DailyScheduleScreen(
                            medication = selectedMedication,
                            onSave = { time, dosage, unit ->
                                saveMedication(
                                    selectedMedication.copy(
                                        doseTime = time,
                                        dosage = dosage.trim(),
                                        dosageUnit = unit.trim()
                                    ),
                                    "details"
                                )
                            },
                            onCancel = { page = "details" }
                        )

                        "reminders" -> ReminderSettingsScreen(
                            medication = selectedMedication,
                            onToggle = { enabled ->
                                saveMedication(selectedMedication.copy(remindersEnabled = enabled), "reminders")
                            },
                            onEditTime = { page = "reminderTime" },
                            onBack = { page = "details" }
                        )

                        "reminderTime" -> ReminderTimeScreen(
                            medication = selectedMedication,
                            onSave = { time ->
                                saveMedication(selectedMedication.copy(reminderTime = time), "reminders")
                            },
                            onCancel = { page = "reminders" }
                        )

                        "supplyDraft" -> SupplyDraftScreen(selectedMedication, onCancel = { page = "details" })

                        else -> TaskPage("MedClock", onBack = { page = "home" }) {
                            Text("Return to the testbed home screen.")
                        }
                    }
                }
            }
        }
    }

    if (dataAction.isNotBlank()) {
        AlertDialog(
            onDismissRequest = { dataAction = "" },
            title = { Text(if (dataAction == "load") "Load sample records?" else "Clear test records?") },
            text = { Text("This replaces the records in this testbed's separate test storage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val loaded = dataAction == "load"
                        val success = if (loaded) store.loadSampleRecords() else store.clearTestRecords()
                        if (success) {
                            medications = store.loadMedications()
                            appointments = store.loadAppointments()
                            selectedId = ""
                            notice = if (loaded) "Sample records loaded." else "Test records cleared."
                        } else {
                            notice = "Test records could not be changed. Please try again."
                        }
                        dataAction = ""
                    },
                    modifier = Modifier.testTag("confirmTestData")
                ) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { dataAction = "" }) { Text("Cancel") } }
        )
    }
}
