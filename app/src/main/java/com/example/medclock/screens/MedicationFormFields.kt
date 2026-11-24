package com.example.medclock.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.data.DOSAGE_UNITS
import com.example.medclock.ui.theme.MedClockTheme

object MedicationFormKeys {
    const val NAME = "name"
    const val DOSAGE = "dosage"
    const val DOSAGE_UNIT = "dosageUnit"
}

@Composable
fun MedicationFormFields(
    name: String,
    onNameChange: (String) -> Unit,
    dosage: String,
    onDosageChange: (String) -> Unit,
    dosageUnit: String,
    onDosageUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errors: Map<String, String> = emptyMap()
) {
    val nameError = errors[MedicationFormKeys.NAME]

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Medication name") },
            placeholder = { Text("e.g. Paracetamol") },
            singleLine = true,
            isError = nameError != null,
            supportingText = if (nameError != null) {
                { Text(nameError) }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("medicationNameInput")
        )

        DosageAndUnitFields(
            dosage = dosage,
            onDosageChange = onDosageChange,
            dosageUnit = dosageUnit,
            onDosageUnitChange = onDosageUnitChange,
            dosageError = errors[MedicationFormKeys.DOSAGE],
            dosageUnitError = errors[MedicationFormKeys.DOSAGE_UNIT]
        )
    }
}


@Composable
fun DosageAndUnitFields(
    dosage: String,
    onDosageChange: (String) -> Unit,
    dosageUnit: String,
    onDosageUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    dosageError: String? = null,
    dosageUnitError: String? = null,
    testTagSuffix: String = ""
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = dosage,
            onValueChange = onDosageChange,
            label = { Text("Dosage") },
            placeholder = { Text("e.g. 500") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = dosageError != null,
            supportingText = if (dosageError != null) {
                { Text(dosageError) }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dosageInput$testTagSuffix")
        )

        DosageUnitDropdown(
            selectedUnit = dosageUnit,
            onUnitSelected = onDosageUnitChange,
            error = dosageUnitError,
            testTag = "dosageUnitInput$testTagSuffix"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosageUnitDropdown(
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    units: List<String> = DOSAGE_UNITS,
    testTag: String = "dosageUnitInput"
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            readOnly = true,
            label = { Text("Dosage unit") },
            placeholder = { Text("Select a unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClickLabel = "Choose dosage unit") { expanded = true }
                .testTag("$testTag-open")
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    },
                    modifier = Modifier.testTag("$testTag-option-$unit")
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationFormFieldsPreview() {
    MedClockTheme {
        MedicationFormFields(
            name = "Paracetamol",
            onNameChange = {},
            dosage = "",
            onDosageChange = {},
            dosageUnit = "mg",
            onDosageUnitChange = {},
            errors = mapOf(MedicationFormKeys.DOSAGE to "Enter a dosage."),
            modifier = Modifier.padding(16.dp)
        )
    }
}
