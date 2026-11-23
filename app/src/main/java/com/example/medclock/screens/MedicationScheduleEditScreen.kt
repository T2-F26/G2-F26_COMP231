
package com.example.medclock.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme

@Composable
fun MedicationScheduleEditScreen(
    medicationName: String,
    savedTimes: List<String>,
    onSave: (List<String>) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Create editable copies of the saved dose times.
    val editedTimes = remember(savedTimes) {
        mutableStateListOf<String>().apply {
            addAll(savedTimes)
        }
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Medication Schedule",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = medicationName,
            style = MaterialTheme.typography.titleMedium
        )

        Text("Enter dose times using 24-hour format (HH:mm).")

        editedTimes.forEachIndexed { index, time ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = {
                        editedTimes[index] = it
                        errorMessage = null
                    },
                    label = { Text("Dose ${index + 1} time") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        editedTimes.removeAt(index)
                        errorMessage = null
                    }
                ) {
                    Text("Remove")
                }
            }
        }

        OutlinedButton(
            onClick = {
                editedTimes.add("")
                errorMessage = null
            }
        ) {
            Text("+ Add Dose Time")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    editedTimes.clear()
                    editedTimes.addAll(savedTimes)
                    errorMessage = null
                    onCancel()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val newTimes = editedTimes.map {
                        it.trim()
                    }

                    val timePattern = Regex(
                        "^([01][0-9]|2[0-3]):[0-5][0-9]$"
                    )

                    errorMessage = when {
                        newTimes.isEmpty() ->
                            "Add at least one dose time."

                        newTimes.any { !timePattern.matches(it) } ->
                            "Enter valid times such as 08:00 or 20:00."

                        newTimes.distinct().size != newTimes.size ->
                            "Duplicate dose times are not allowed."

                        else -> null
                    }

                    if (errorMessage == null) {
                        onSave(newTimes)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationScheduleEditPreview() {
    var savedTimes by remember {
        mutableStateOf(listOf("08:00", "20:00"))
    }

    MedClockTheme {
        MedicationScheduleEditScreen(
            medicationName = "Paracetamol",
            savedTimes = savedTimes,
            onSave = { updatedTimes ->
                savedTimes = updatedTimes
            },
            onCancel = {}
        )
    }
}
