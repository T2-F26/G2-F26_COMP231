package com.example.medclock.josefinademo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medclock.data.MedicationName
import com.example.medclock.data.MedicationNameSource
import com.example.medclock.data.savedMedicationNames
import com.example.medclock.reminders.DraftValue
import com.example.medclock.reminders.ReminderTimeField
import com.example.medclock.screens.AddMedicationForm
import com.example.medclock.screens.AppointmentDetailsScreen
import com.example.medclock.screens.DailyMedicationScheduleScreen
import com.example.medclock.screens.DeleteMedicationConfirmationDialog
import com.example.medclock.screens.DosageUnitDropdown
import com.example.medclock.screens.EditMedicationDetailsScreen
import com.example.medclock.screens.MedicationFormKeys
import com.example.medclock.screens.ReminderTimingCancelButton
import com.example.medclock.screens.RemainingSupplyText
import com.example.medclock.screens.SavedMedicationNamesList
import com.example.medclock.screens.ScheduledDoseInput
import com.example.medclock.supply.DoseConfirmation
import com.example.medclock.supply.SupplyInputValidator
import com.example.medclock.supply.estimateRemainingSupply
import com.example.medclock.ui.theme.MedClockTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class JosefinaDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedClockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JosefinaDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private data class DemoMedication(
    val id: String,
    val name: String,
    val dosage: String,
    val dosageUnit: String,
    val reminderTime: String
)

private fun sampleMedications() = listOf(
    DemoMedication("med-1", "Metformin", "500", "mg", "08:00"),
    DemoMedication("med-2", "Ibuprofen", "200", "mg", "13:00"),
    DemoMedication("med-3", "Vitamin D", "1000", "IU", "21:30")
)

private enum class DemoPage(val title: String) {
    MENU("MedClock - Josefina's tasks"),
    NAMES("Story 1: Saved medication names"),
    ADD("Story 2: Add medication form"),
    SCHEDULE("Story 3: Daily medication schedule"),
    EDIT("Story 4: Edit medication details"),
    DELETE("Story 5: Delete confirmation"),
    REMINDER_TIME("Story 6: Choose a new reminder time"),
    SUPPLY("Stories 7 and 8: Remaining supply"),
    APPOINTMENT("Story 9: Appointment details"),
    REMINDER_TIMING("Story 10: Cancel reminder timing")
}

@Composable
private fun JosefinaDemo(modifier: Modifier = Modifier) {
    var page by rememberSaveable { mutableStateOf(DemoPage.MENU) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val medications = remember { mutableStateListOf(*sampleMedications().toTypedArray()) }
    val selected = medications.firstOrNull { it.id == selectedId } ?: medications.firstOrNull()

    val openMenu: () -> Unit = { page = DemoPage.MENU }
    val showMessage: (String) -> Unit = { message = it }
    val restoreSamples: () -> Unit = {
        medications.clear()
        medications.addAll(sampleMedications())
    }
    val replaceMedication: (DemoMedication) -> Unit = { updated ->
        val index = medications.indexOfFirst { it.id == updated.id }
        if (index >= 0) medications[index] = updated
    }

    BackHandler(enabled = page != DemoPage.MENU, onBack = openMenu)

    when (page) {
        DemoPage.MENU -> DemoMenu(onOpen = { page = it }, modifier = modifier)

        DemoPage.APPOINTMENT -> AppointmentDetailsScreen(
            doctorName = "Dr. Maria Lopez",
            date = LocalDate.now().plusDays(3).toString(),
            time = "14:30",
            location = "Downtown Family Clinic, Room 204",
            onBack = openMenu,
            modifier = modifier
        )

        else -> DemoPageFrame(title = page.title, onBack = openMenu, modifier = modifier) {
            when (page) {
                DemoPage.NAMES -> NamesDemo(
                    medications = medications,
                    onSelect = {
                        selectedId = it.id
                        showMessage("${it.name} is now used in the schedule, edit, and reminder demos.")
                    },
                    onClear = { medications.clear() },
                    onRestore = restoreSamples
                )

                DemoPage.ADD -> AddDemo(
                    onSave = { name, dosage, dosageUnit ->
                        val added = DemoMedication(UUID.randomUUID().toString(), name, dosage, dosageUnit, "")
                        medications.add(added)
                        selectedId = added.id
                        page = DemoPage.NAMES
                        showMessage("$name $dosage $dosageUnit was added to the list.")
                    },
                    onCancel = openMenu
                )

                DemoPage.SCHEDULE -> if (selected == null) {
                    NoMedicationMessage(onRestore = restoreSamples)
                } else {
                    ScheduleDemo(medication = selected, showMessage = showMessage, onCancel = openMenu)
                }

                DemoPage.EDIT -> if (selected == null) {
                    NoMedicationMessage(onRestore = restoreSamples)
                } else {
                    EditDemo(
                        medication = selected,
                        onSave = { updated ->
                            replaceMedication(updated)
                            page = DemoPage.NAMES
                            showMessage("Saved: ${updated.name} ${updated.dosage} ${updated.dosageUnit}")
                        },
                        onCancel = openMenu
                    )
                }

                DemoPage.DELETE -> DeleteDemo(
                    medications = medications,
                    onDelete = { deleted ->
                        medications.remove(deleted)
                        showMessage("${deleted.name} was deleted.")
                    }
                )

                DemoPage.REMINDER_TIME -> if (selected == null) {
                    NoMedicationMessage(onRestore = restoreSamples)
                } else {
                    ReminderTimeDemo(
                        medication = selected,
                        onTimeSelected = { time ->
                            replaceMedication(selected.copy(reminderTime = time))
                            showMessage("The ${selected.name} reminder is now set for $time.")
                        }
                    )
                }

                DemoPage.SUPPLY -> SupplyDemo()

                DemoPage.REMINDER_TIMING -> ReminderTimingDemo()

                else -> Unit
            }
        }
    }

    message?.let { text ->
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("MedClock demo") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun DemoMenu(onOpen: (DemoPage) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(DemoPage.MENU.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            "Debug-only demo of Josefina's Iteration 1 tasks. It uses sample data kept in memory, " +
                    "so nothing is saved to the phone.",
            style = MaterialTheme.typography.bodyMedium
        )
        DemoPage.entries.filter { it != DemoPage.MENU }.forEach { page ->
            OutlinedButton(onClick = { onOpen(page) }, modifier = Modifier.fillMaxWidth()) {
                Text(page.title)
            }
        }
    }
}

@Composable
private fun DemoPageFrame(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("Back to menu")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun NoMedicationMessage(onRestore: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("There are no medications left in the demo.")
        Button(onClick = onRestore) { Text("Restore sample medications") }
    }
}

@Composable
private fun NamesDemo(
    medications: List<DemoMedication>,
    onSelect: (MedicationName) -> Unit,
    onClear: () -> Unit,
    onRestore: () -> Unit
) {
    val source = MedicationNameSource { medications.map { MedicationName(it.id, it.name) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Tap a medication to use it in the schedule, edit, and reminder demos.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onClear) { Text("Clear list") }
            OutlinedButton(onClick = onRestore) { Text("Restore samples") }
        }
        SavedMedicationNamesList(
            medications = source.savedMedicationNames(),
            onMedicationClick = onSelect,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            emptyContent = { Text("No medications saved yet.") }
        )
    }
}

@Composable
private fun AddDemo(
    onSave: (name: String, dosage: String, dosageUnit: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var dosageUnit by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }

    AddMedicationForm(
        name = name,
        onNameChange = { name = it },
        dosage = dosage,
        onDosageChange = { dosage = it },
        dosageUnit = dosageUnit,
        onDosageUnitChange = { dosageUnit = it },
        errors = errors,
        actions = {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = {
                    errors = requiredFieldErrors(name, dosage, dosageUnit)
                    if (errors.isEmpty()) onSave(name.trim(), dosage.trim(), dosageUnit)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    )
}

private fun requiredFieldErrors(name: String, dosage: String, dosageUnit: String): Map<String, String> =
    buildMap {
        if (name.isBlank()) put(MedicationFormKeys.NAME, "Enter a medication name.")
        if (dosage.isBlank()) put(MedicationFormKeys.DOSAGE, "Enter a dosage.")
        if (dosageUnit.isBlank()) put(MedicationFormKeys.DOSAGE_UNIT, "Choose a dosage unit.")
    }

@Composable
private fun ScheduleDemo(
    medication: DemoMedication,
    showMessage: (String) -> Unit,
    onCancel: () -> Unit
) {
    val doses = remember(medication.id) {
        mutableStateListOf(
            ScheduledDoseInput(
                time = medication.reminderTime,
                dosage = medication.dosage,
                dosageUnit = medication.dosageUnit
            )
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    DailyMedicationScheduleScreen(
        medicationName = medication.name,
        doses = doses,
        onDoseChange = { index, dose ->
            doses[index] = dose
            error = null
        },
        onAddDose = {
            doses.add(ScheduledDoseInput(dosage = medication.dosage, dosageUnit = medication.dosageUnit))
            error = null
        },
        onRemoveDose = {
            doses.removeAt(it)
            error = null
        },
        errorMessage = error,
        actions = {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = {
                    error = when {
                        doses.isEmpty() -> "Add at least one dose time."
                        doses.any { it.time.isBlank() } -> "Choose a time for every dose."
                        else -> null
                    }
                    if (error == null) {
                        showMessage(
                            "${medication.name} schedule: " +
                                    doses.joinToString { "${it.time} (${it.dosage} ${it.dosageUnit})" }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun EditDemo(
    medication: DemoMedication,
    onSave: (DemoMedication) -> Unit,
    onCancel: () -> Unit
) {
    var errors by remember { mutableStateOf(emptyMap<String, String>()) }

    EditMedicationDetailsScreen(
        currentName = medication.name,
        currentDosage = medication.dosage,
        currentDosageUnit = medication.dosageUnit,
        onSave = { name, dosage, dosageUnit ->
            errors = requiredFieldErrors(name, dosage, dosageUnit)
            if (errors.isEmpty()) {
                onSave(medication.copy(name = name, dosage = dosage, dosageUnit = dosageUnit))
            }
        },
        onCancel = onCancel,
        errors = errors
    )
}

@Composable
private fun DeleteDemo(
    medications: List<DemoMedication>,
    onDelete: (DemoMedication) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<DemoMedication?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Tap Delete next to a medication to see the confirmation message.")
        if (medications.isEmpty()) {
            Text("No medications saved yet.")
        }
        medications.forEach { medication ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(medication.name, modifier = Modifier.weight(1f))
                TextButton(onClick = { pendingDelete = medication }) { Text("Delete") }
            }
            HorizontalDivider()
        }
    }

    pendingDelete?.let { medication ->
        DeleteMedicationConfirmationDialog(
            medicationName = medication.name,
            onConfirm = {
                pendingDelete = null
                onDelete(medication)
            },
            onCancel = { pendingDelete = null }
        )
    }
}

@Composable
private fun ReminderTimeDemo(
    medication: DemoMedication,
    onTimeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(medication.name, style = MaterialTheme.typography.titleLarge)
        Text("The saved time only changes when you press OK in the clock dialog.")
        ReminderTimeField(
            currentTime = medication.reminderTime,
            onTimeSelected = onTimeSelected
        )
    }
}

@Composable
private fun SupplyDemo() {
    var quantityText by rememberSaveable { mutableStateOf("30") }
    var unit by rememberSaveable { mutableStateOf("tablets") }
    var doseText by rememberSaveable { mutableStateOf("1") }

    val periodEnd = remember { LocalDateTime.now() }
    val periodStart = periodEnd.minusDays(7)
    val confirmations = remember(periodEnd) {
        listOf(
            DoseConfirmation(periodEnd.minusDays(1), takenOnTime = true),
            DoseConfirmation(periodEnd.minusDays(2), takenOnTime = true),
            DoseConfirmation(periodEnd.minusDays(3), takenOnTime = true),
            DoseConfirmation(periodEnd.minusDays(4), takenOnTime = false),
            DoseConfirmation(periodEnd.minusDays(10), takenOnTime = true)
        )
    }
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEE MMM d, HH:mm") }

    val errors = SupplyInputValidator.validate(quantityText, unit, expectedUnit = "tablets")
    val quantityError = errors[SupplyInputValidator.QUANTITY]
    val dosePerIntake = doseText.trim().toDoubleOrNull()
    val doseError = if (dosePerIntake == null || dosePerIntake <= 0) "Enter a dose greater than zero." else null
    val remaining = if (errors.isNotEmpty() || dosePerIntake == null || doseError != null) {
        null
    } else {
        estimateRemainingSupply(
            currentSupply = quantityText.trim().toDouble(),
            dosePerIntake = dosePerIntake,
            confirmations = confirmations,
            periodStart = periodStart,
            periodEnd = periodEnd
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Metformin (dosage unit: tablets)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it },
            label = { Text("Current supply") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = quantityError != null,
            supportingText = if (quantityError != null) {
                { Text(quantityError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )
        DosageUnitDropdown(
            selectedUnit = unit,
            onUnitSelected = { unit = it },
            error = errors[SupplyInputValidator.UNIT]
        )
        OutlinedTextField(
            value = doseText,
            onValueChange = { doseText = it },
            label = { Text("Dose per intake") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = doseError != null,
            supportingText = if (doseError != null) {
                { Text(doseError) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()
        Text("Dose confirmations (counting the last 7 days)", style = MaterialTheme.typography.titleSmall)
        confirmations.forEach { confirmation ->
            val status = when {
                confirmation.takenAt.isBefore(periodStart) -> "older than 7 days, not counted"
                confirmation.takenOnTime -> "taken on time, counted"
                else -> "taken late, not counted"
            }
            Text("${confirmation.takenAt.format(dateFormat)}: $status")
        }
        HorizontalDivider()

        RemainingSupplyText(
            remainingQuantity = remaining,
            unit = unit,
            notRecordedText = if (quantityText.isBlank()) "Not recorded" else "Fix the errors above"
        )
    }
}

private val REMINDER_TIMING_OPTIONS = listOf(
    "15 minutes before",
    "30 minutes before",
    "1 hour before",
    "1 day before",
    "2 days before"
)

@Composable
private fun ReminderTimingDemo() {
    var timing by remember { mutableStateOf(DraftValue(saved = "1 day before")) }
    var formOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Appointment with Dr. Maria Lopez", style = MaterialTheme.typography.titleMedium)
        Text("Saved reminder: ${timing.saved}")

        if (!formOpen) {
            Button(onClick = { formOpen = true }) { Text("Change reminder timing") }
        } else {
            Text("When should MedClock remind you?", style = MaterialTheme.typography.titleSmall)
            Column(modifier = Modifier.selectableGroup()) {
                REMINDER_TIMING_OPTIONS.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = timing.draft == option,
                                onClick = { timing = timing.update(option) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = timing.draft == option, onClick = null)
                        Text(option, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReminderTimingCancelButton(
                    timing = timing,
                    onCancel = {
                        timing = it
                        formOpen = false
                    },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        timing = timing.commit()
                        formOpen = false
                    },
                    enabled = timing.hasChanges,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
