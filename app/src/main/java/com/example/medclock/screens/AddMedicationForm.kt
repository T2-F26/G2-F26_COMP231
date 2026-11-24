package com.example.medclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun AddMedicationForm(
    name: String,
    onNameChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    dosageUnit: String,
    onDosageUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errors: Map<String, String> = emptyMap(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add a new medication",
            style = MaterialTheme.typography.headlineSmall
        )

        MedicationFormFields(
            name = name,
            onNameChange = onNameChange,
            dosage = dosage,
            onDosageChange = onDosageChange,
            dosageUnit = dosageUnit,
            onDosageUnitChange = onDosageUnitChange,
            errors = errors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = actions
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMedicationFormPreview() {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("") }

    MedClockTheme {
        AddMedicationForm(
            name = name,
            onNameChange = { name = it },
            dosage = dosage,
            onDosageChange = { dosage = it },
            dosageUnit = dosageUnit,
            onDosageUnitChange = { dosageUnit = it },
            actions = {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save") }
            }
        )
    }
}
