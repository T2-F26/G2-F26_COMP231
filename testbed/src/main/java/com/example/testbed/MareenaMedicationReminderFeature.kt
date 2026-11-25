
package com.example.medclock.mareena

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.data.MedClockDemoMedicationStore
import com.example.medclock.data.MedicationReminderStatusStore
import com.example.medclock.data.SavedMedication
import com.example.medclock.reminders.MedicationReminderScheduler

/**
 * Mareena's medication reminder management screen.
 *
 * Related tasks:
 * #123 - Reminder feature for saved medications.
 * #150 - Cancel future notifications when disabled.
 *
 * Reuses existing MedClock medication data and
 * reminder scheduling logic without modifying them.
 */
@Composable
fun MareenaMedicationReminderScreen(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val medicationStore = remember(context) {
        MedClockDemoMedicationStore(context)
    }

    val statusStore = remember(context) {
        MedicationReminderStatusStore(context)
    }

    val scheduler = remember(context) {
        MedicationReminderScheduler(context)
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    var feedback by remember {
        mutableStateOf<String?>(null)
    }

    val medications = remember(refreshKey) {
        medicationStore.getMedications()
    }

    val savedStatuses = remember(refreshKey) {
        medications.associate { medication ->
            medication.id to (
                    statusStore.getReminderEnabled(
                        medication.id
                    ) ?: true
                    )
        }
    }

    var notificationPermissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            notificationPermissionGranted = granted

            feedback = if (granted) {
                "Notification permission granted."
            } else {
                "Notification permission was denied."
            }
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Text(
            "Medication Reminders",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Enable or disable reminders for each " +
                    "saved medication."
        )

        if (!notificationPermissionGranted) {

            Text(
                "Notifications are not permitted. " +
                        "Enable notification access to receive alerts.",
                color = MaterialTheme.colorScheme.error
            )

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {

                Button(
                    onClick = {
                        permissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                ) {
                    Text("Allow Notifications")
                }
            }
        }

        OutlinedButton(
            onClick = {
                refreshKey++
                feedback = null
            }
        ) {
            Text("Refresh Medications")
        }

        feedback?.let { message ->

            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider()

        if (medications.isEmpty()) {

            Text("No saved medications found.")

        } else {

            medications.forEach { medication ->

                MareenaReminderCard(
                    medication = medication,
                    savedEnabled =
                        savedStatuses[medication.id] ?: true,
                    onSave = { enabled ->

                        if (
                            enabled &&
                            medication.doseTimes.isEmpty()
                        ) {

                            feedback =
                                "Set daily dose times for " +
                                        medication.name +
                                        " before enabling reminders."

                        } else {

                            val saved =
                                scheduler.setReminderEnabled(
                                    medication = medication,
                                    enabled = enabled
                                )

                            refreshKey++

                            feedback = if (saved) {

                                if (enabled) {
                                    "Reminder settings saved. " +
                                            "Alarms requested for " +
                                            medication.name + "."
                                } else {
                                    "Reminders disabled and " +
                                            "known future alarms " +
                                            "cancelled for " +
                                            medication.name + "."
                                }

                            } else {

                                "Could not complete reminder " +
                                        "scheduling. Check notification " +
                                        "and alarm permissions."
                            }
                        }
                    }
                )
            }
        }

        HorizontalDivider()

        Text(
            "Reminder settings are saved for each " +
                    "medication separately.",
            style = MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * One medication reminder control.
 *
 * The switch changes a pending choice.
 * The setting changes only when Save is pressed.
 */
@Composable
private fun MareenaReminderCard(
    medication: SavedMedication,
    savedEnabled: Boolean,
    onSave: (Boolean) -> Unit
) {

    var pendingEnabled by remember(
        medication.id,
        savedEnabled
    ) {
        mutableStateOf(savedEnabled)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Text(
                medication.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Dosage: ${medication.dosage}")

            Text(
                "Daily Schedule: " +
                        if (medication.doseTimes.isEmpty()) {
                            "Not set"
                        } else {
                            medication.doseTimes.joinToString(", ")
                        }
            )

            Text(
                "Saved Preference: " +
                        if (savedEnabled) {
                            "Enabled"
                        } else {
                            "Disabled"
                        }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    androidx.compose.ui.Alignment.CenterVertically
            ) {

                Text("Enable Reminders")

                Switch(
                    checked = pendingEnabled,
                    onCheckedChange = {
                        pendingEnabled = it
                    }
                )
            }

            Button(
                onClick = {
                    onSave(pendingEnabled)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Reminder Setting")
            }
        }
    }
}

/**
 * Standalone Compose Preview.
 *
 * Preview does not write to medication storage
 * or schedule actual Android alarms.
 */
@Preview(showBackground = true)
@Composable
private fun MareenaReminderPreview() {

    MaterialTheme {

        MareenaReminderCard(
            medication = SavedMedication(
                id = "preview-medication",
                name = "Paracetamol",
                dosage = "500 mg",
                doseTimes = listOf(
                    "08:00",
                    "20:00"
                )
            ),
            savedEnabled = true,
            onSave = {}
        )
    }
}
