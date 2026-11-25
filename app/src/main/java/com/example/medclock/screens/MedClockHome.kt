package com.example.medclock.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medclock.data.MedClockDemoMedicationStore
import com.example.medclock.data.MedicationReminderStatusStore
import com.example.medclock.reminders.MedicationReminderScheduler
import com.example.medclock.schedule.DemoMedicationScheduleSource
import com.example.medclock.schedule.UpcomingDoseFinder
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
fun MedClockHome(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { MedClockDemoMedicationStore(context) }
    val statusStore = remember(context) { MedicationReminderStatusStore(context) }
    val scheduler = remember(context) { MedicationReminderScheduler(context) }

    var medications by remember(store) { mutableStateOf(store.getMedications()) }
    var selectedMedicationId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSupply by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var supplyInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    var notificationPermissionGranted by remember(context) {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        if (!granted) {
            message = "Android notification permission is off. Dose times will be saved, " +
                    "but MedClock cannot show reminder notifications."
        }
    }

    fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationPermissionGranted = true
        }
    }

    // Re-arm reminders previously saved before this screen was connected.
    LaunchedEffect(scheduler) {
        scheduler.restoreAllReminders()
    }

    val selectedMedication = medications.firstOrNull { it.id == selectedMedicationId }

    when {
        // SCREEN 1: List of saved medications.
        selectedMedication == null -> {
            Box(modifier = modifier.fillMaxSize()) {
                MedicationListScreen(
                    medications = medications,
                    onMedicationSelected = { medication ->
                        selectedMedicationId = medication.id
                        editingSupply = false
                        editingSchedule = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Text("Add Medication")
                }
            }
        }

        // SCREEN 2: Edit daily dose times.
        editingSchedule -> {
            MedicationScheduleEditScreen(
                medicationName = selectedMedication.name,
                savedTimes = selectedMedication.doseTimes,
                onSave = { updatedTimes ->
                    val saved = store.updateSchedule(selectedMedication.id, updatedTimes)
                    if (saved) {
                        val updated = store.getMedications()
                            .first { it.id == selectedMedication.id }
                        val alarmsUpdated = scheduler.rescheduleMedication(
                            oldMedication = selectedMedication,
                            updatedMedication = updated
                        )
                        medications = store.getMedications()
                        editingSchedule = false
                        if (!alarmsUpdated) {
                            message = "Dose times were saved, but one or more reminders " +
                                    "could not be scheduled. Check alarm access."
                        } else {
                            requestNotificationPermissionIfNeeded()
                        }
                    } else {
                        message = "Could not save the dose times. Use unique 24-hour times like 08:00."
                    }
                },
                onCancel = { editingSchedule = false },
                modifier = modifier
            )
        }

        // SCREEN 3: Edit medication supply.
        editingSupply -> {
            MedicationSupplyScreen(
                medicationName = selectedMedication.name,
                quantity = supplyInput,
                unit = selectedMedication.supplyUnit,
                onQuantityChange = { supplyInput = it },
                onSave = {
                    val saved = store.updateSupply(
                        medicationId = selectedMedication.id,
                        quantityInput = supplyInput,
                        unit = selectedMedication.supplyUnit
                    )
                    if (saved) {
                        medications = store.getMedications()
                        editingSupply = false
                    } else {
                        message = "Enter a valid non-negative supply quantity."
                    }
                },
                onCancel = { editingSupply = false },
                modifier = modifier
            )
        }

        // SCREEN 4: Medication details and reminder controls.
        else -> {
            var remindersEnabled by remember(selectedMedication.id) {
                mutableStateOf(statusStore.getReminderEnabled(selectedMedication.id) ?: true)
            }

            val nextDose = UpcomingDoseFinder(
                DemoMedicationScheduleSource(
                    medicationStore = store,
                    reminderStore = statusStore
                )
            ).findUpcomingDoses().firstOrNull {
                it.medicationId == selectedMedication.id
            }

            val exactAlarmAvailable = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    (context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager)
                        .canScheduleExactAlarms()

            Column(modifier = modifier.fillMaxSize()) {
                TextButton(
                    onClick = {
                        selectedMedicationId = null
                        editingSupply = false
                        editingSchedule = false
                    }
                ) {
                    Text("Back to Medications")
                }

                Text(
                    text = selectedMedication.supplyQuantity?.let {
                        "Current Supply: $it ${selectedMedication.supplyUnit}"
                    } ?: "Current Supply: Not recorded",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                selectedMedication.stockUpdatedAtMillis?.let { timestamp ->
                    Text(
                        text = "Updated: " + DateFormat.getDateTimeInstance()
                            .format(Date(timestamp)),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Medication Reminders", modifier = Modifier.weight(1f))
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { enabled ->
                            val saved = scheduler.setReminderEnabled(selectedMedication, enabled)
                            if (saved) {
                                remindersEnabled = enabled
                                if (enabled) requestNotificationPermissionIfNeeded()
                            } else {
                                message = "Could not update reminder settings or alarms."
                            }
                        }
                    )
                }

                Text(
                    text = if (!remindersEnabled) {
                        "Next Dose: Reminders off"
                    } else if (nextDose != null) {
                        "Next Dose: " + nextDose.scheduledAt.format(
                            DateTimeFormatter.ofPattern("MMM d, h:mm a")
                        )
                    } else {
                        "Next Dose: Not scheduled"
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                if (remindersEnabled && !notificationPermissionGranted &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ) {
                    TextButton(
                        onClick = { requestNotificationPermissionIfNeeded() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Allow Android Notifications")
                    }
                }

                if (remindersEnabled && !exactAlarmAvailable) {
                    TextButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                                runCatching { context.startActivity(intent) }
                                    .onFailure {
                                        message = "Open Android Settings to allow exact alarms " +
                                                "for MedClock, if available."
                                    }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("Allow On-Time Alarms")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        supplyInput = selectedMedication.supplyQuantity?.toString() ?: ""
                        editingSupply = true
                    }) {
                        Text("Update Supply")
                    }
                    Button(onClick = { editingSchedule = true }) {
                        Text("Edit Schedule")
                    }
                }

                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Delete Medication")
                }

                MedicationDetailsScreen(
                    medicationName = selectedMedication.name,
                    dosage = selectedMedication.dosage,
                    schedule = selectedMedication.doseTimes.joinToString(", ")
                        .ifBlank { "Not set" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showDeleteDialog && selectedMedication != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication?") },
            text = {
                Text("Delete ${selectedMedication.name} and cancel its future reminders?")
            },
            confirmButton = {
                TextButton(onClick = {
                    val deleted = scheduler.deleteMedicationWithReminders(selectedMedication)
                    showDeleteDialog = false
                    if (deleted) {
                        medications = store.getMedications()
                        selectedMedicationId = null
                        editingSupply = false
                        editingSchedule = false
                    } else {
                        message = "Could not delete this medication."
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (message != null) {
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("MedClock") },
            text = { Text(message.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK") }
            }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("tablets") }
        val validUnit = unit.trim().lowercase() in setOf("tablets", "capsules", "ml")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Medication") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medication Name") }
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") }
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Supply Unit") },
                        supportingText = { Text("tablets, capsules, or mL") }
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && dosage.isNotBlank() && validUnit,
                    onClick = {
                        store.addMedication(name, dosage, unit)
                        medications = store.getMedications()
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
