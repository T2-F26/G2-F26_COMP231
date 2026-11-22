package com.example.testbed.medclock

import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.util.Locale

@Composable
fun TaskPage(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}

@Composable
fun MedicationListScreen(
    medications: List<Medication>,
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Medications", style = MaterialTheme.typography.headlineSmall)
        if (medications.isEmpty()) {
            MedicationEmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(medications, key = { it.id }) { medication ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                            .testTag("medication-${medication.id}")
                            .clickable { onOpen(medication.id) }
                    ) {
                        Text(medication.name, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MedicationEmptyState() {
    Text("No medications saved yet.", modifier = Modifier.testTag("emptyMedicationList"))
}

@Composable
fun DetailValue(label: String, value: String, tag: String = label) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(value, modifier = Modifier.testTag(tag))
    }
}

@Composable
fun MedicationDetailsScreen(
    medication: Medication,
    onEdit: () -> Unit,
    onSchedule: () -> Unit,
    onReminders: () -> Unit,
    onSupply: () -> Unit,
    onBack: () -> Unit
) {
    TaskPage("Medication details", onBack) {
        DetailValue("Medication name", medication.name, "savedMedicationName")
        DetailValue(
            "Dosage",
            "${medication.dosage} ${medication.dosageUnit}",
            "savedMedicationDosage"
        )
        DetailValue("Daily dose time", medication.doseTime.ifBlank { "Schedule not set" })
        DetailValue("Current supply", medication.supply?.toString() ?: "Not recorded", "savedSupply")
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth().testTag("editMedication")) {
            Text("Edit medication")
        }
        OutlinedButton(onClick = onSchedule, modifier = Modifier.fillMaxWidth().testTag("editSchedule")) {
            Text(if (medication.doseTime.isBlank()) "Set daily schedule" else "Edit daily schedule")
        }
        OutlinedButton(onClick = onReminders, modifier = Modifier.fillMaxWidth().testTag("openReminders")) {
            Text("Reminder settings")
        }
        OutlinedButton(onClick = onSupply, modifier = Modifier.fillMaxWidth().testTag("openSupplyDraft")) {
            Text("Open supply draft")
        }
    }
}

@Composable
fun MedicationEditScreen(
    medication: Medication,
    onSave: (String, String, String) -> String?,
    onCancel: () -> Unit
) {
    var name by rememberSaveable(medication.id) { mutableStateOf(medication.name) }
    var dosage by rememberSaveable(medication.id) { mutableStateOf(medication.dosage) }
    var unit by rememberSaveable(medication.id) { mutableStateOf(medication.dosageUnit) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    TaskPage("Correct medication details", onCancel) {
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Medication name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("editName")
        )
        DosageFields(dosage, { dosage = it }, unit, { unit = it })
        ErrorText(error)
        Button(
            onClick = {
                error = medicationDetailsError(name, dosage, unit)
                if (error == null) error = onSave(name, dosage, unit)
            },
            modifier = Modifier.fillMaxWidth().testTag("saveMedication")
        ) { Text("Save changes") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("cancelMedication")) {
            Text("Cancel")
        }
    }
}

@Composable
fun DosageFields(
    dosage: String,
    onDosageChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit
) {
    OutlinedTextField(
        value = dosage, onValueChange = onDosageChange,
        label = { Text("Dosage") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag("dosageInput")
    )
    OutlinedTextField(
        value = unit, onValueChange = onUnitChange,
        label = { Text("Dosage unit") }, singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("dosageUnitInput")
    )
}

@Composable
fun TimeField(value: String, onChange: (String) -> Unit, tag: String) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value, onValueChange = onChange,
            label = { Text("Time (HH:mm)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(tag)
        )
        TextButton(
            onClick = {
                val initial = if (isValidTime(value)) LocalTime.parse(value) else LocalTime.of(8, 0)
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onChange(String.format(Locale.ROOT, "%02d:%02d", hour, minute)) },
                    initial.hour, initial.minute, true
                ).show()
            }
        ) { Text("Choose time") }
    }
}

@Composable
fun DailyScheduleFields(
    medicationName: String,
    time: String,
    onTimeChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit
) {
    OutlinedTextField(
        value = medicationName, onValueChange = {}, readOnly = true,
        label = { Text("Medication name") }, singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("scheduleMedicationName")
    )
    TimeField(time, onTimeChange, "scheduleTime")
    DosageFields(dosage, onDosageChange, unit, onUnitChange)
}

@Composable
fun DailyScheduleScreen(
    medication: Medication,
    onSave: (String, String, String) -> String?,
    onCancel: () -> Unit
) {
    var time by rememberSaveable(medication.id) { mutableStateOf(medication.doseTime) }
    var dosage by rememberSaveable(medication.id) { mutableStateOf(medication.dosage) }
    var unit by rememberSaveable(medication.id) { mutableStateOf(medication.dosageUnit) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    TaskPage(if (medication.doseTime.isBlank()) "Set daily schedule" else "Edit daily schedule", onCancel) {
        DailyScheduleFields(medication.name, time, { time = it }, dosage, { dosage = it }, unit, { unit = it })
        ErrorText(error)
        Button(
            onClick = {
                error = medicationDetailsError(medication.name, dosage, unit)
                if (error == null && !isValidTime(time)) error = "Enter a valid time, for example 08:30."
                if (error == null) error = onSave(time, dosage, unit)
            },
            modifier = Modifier.fillMaxWidth().testTag("saveSchedule")
        ) { Text("Save schedule") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("cancelSchedule")) {
            Text("Cancel")
        }
    }
}

@Composable
fun ReminderSettingsScreen(
    medication: Medication,
    onToggle: (Boolean) -> String?,
    onEditTime: () -> Unit,
    onBack: () -> Unit
) {
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    TaskPage("Medication reminder settings", onBack) {
        Text(medication.name, style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Medication reminders")
                Text(if (medication.remindersEnabled) "Enabled" else "Disabled")
            }
            Switch(
                checked = medication.remindersEnabled,
                onCheckedChange = { error = onToggle(it) },
                modifier = Modifier.testTag("remindersSwitch")
            )
        }
        DetailValue("Saved reminder time", medication.reminderTime.ifBlank { "Not set" }, "savedReminderTime")
        Button(onClick = onEditTime, modifier = Modifier.fillMaxWidth().testTag("editReminderTime")) {
            Text("Edit reminder time")
        }
        ErrorText(error)
    }
}

@Composable
fun ReminderTimeScreen(
    medication: Medication,
    onSave: (String) -> String?,
    onCancel: () -> Unit
) {
    var time by rememberSaveable(medication.id) { mutableStateOf(medication.reminderTime) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    TaskPage("Change reminder time", onCancel) {
        Text(medication.name)
        TimeField(time, { time = it }, "reminderTimeInput")
        ErrorText(error)
        Button(
            onClick = {
                error = if (!isValidTime(time)) "Enter a valid time, for example 07:55." else onSave(time)
            },
            modifier = Modifier.fillMaxWidth().testTag("saveReminderTime")
        ) { Text("Save reminder time") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("cancelReminderTime")) {
            Text("Cancel")
        }
    }
}

@Composable
fun SupplyDraftScreen(medication: Medication, onCancel: () -> Unit) {
    var quantity by rememberSaveable(medication.id) { mutableStateOf(medication.supply?.toString().orEmpty()) }
    var unit by rememberSaveable(medication.id) { mutableStateOf(medication.dosageUnit) }
    TaskPage("Medication supply draft", onCancel) {
        Text(medication.name)
        DetailValue("Saved supply", medication.supply?.toString() ?: "Not recorded")
        OutlinedTextField(
            value = quantity, onValueChange = { quantity = it },
            label = { Text("Quantity") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("supplyQuantityInput")
        )
        OutlinedTextField(
            value = unit, onValueChange = { unit = it },
            label = { Text("Dosage unit") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("supplyUnitInput")
        )
        Text("Cancel discards this draft and keeps the saved supply.")
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("cancelSupply")) {
            Text("Cancel")
        }
    }
}

@Composable
fun ErrorText(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
}
