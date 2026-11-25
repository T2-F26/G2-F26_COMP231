
package com.example.medclock.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme

@Composable
fun MedicationSupplyScreen(
    medicationName: String,
    quantity: String,
    unit: String,
    onQuantityChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Medication Supply",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Medication: $medicationName",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Current Supply") },
            placeholder = { Text("Enter quantity") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Unit: $unit",
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationSupplyScreenPreview() {
    var quantity by remember { mutableStateOf("30") }

    MedClockTheme {
        MedicationSupplyScreen(
            medicationName = "Paracetamol",
            quantity = quantity,
            unit = "tablets",
            onQuantityChange = { quantity = it },
            onSave = {},
            onCancel = {}
        )
    }
}
