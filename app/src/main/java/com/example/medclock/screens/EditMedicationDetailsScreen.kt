package com.example.medclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun EditMedicationDetailsScreen(
    currentName: String,
    currentDosage: String,
    currentDosageUnit: String,
    onSave: (name: String, dosage: String, dosageUnit: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    errors: Map<String, String> = emptyMap()
) {
    var name by rememberSaveable(currentName) { mutableStateOf(currentName) }
    var dosage by rememberSaveable(currentDosage) { mutableStateOf(currentDosage) }
    var dosageUnit by rememberSaveable(currentDosageUnit) { mutableStateOf(currentDosageUnit) }

    val hasChanges = name.trim() != currentName ||
            dosage.trim() != currentDosage ||
            dosageUnit != currentDosageUnit

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit medication",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Change the name, dosage, or dosage unit, then save.",
            style = MaterialTheme.typography.bodyMedium
        )

        MedicationFormFields(
            name = name,
            onNameChange = { name = it },
            dosage = dosage,
            onDosageChange = { dosage = it },
            dosageUnit = dosageUnit,
            onDosageUnitChange = { dosageUnit = it },
            errors = errors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancelEditMedication")
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(name.trim(), dosage.trim(), dosageUnit) },
                enabled = hasChanges,
                modifier = Modifier
                    .weight(1f)
                    .testTag("saveEditMedication")
            ) {
                Text("Save changes")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditMedicationDetailsScreenPreview() {
    MedClockTheme {
        EditMedicationDetailsScreen(
            currentName = "Ibuprofen",
            currentDosage = "200",
            currentDosageUnit = "mg",
            onSave = { _, _, _ -> },
            onCancel = {}
        )
    }
}
