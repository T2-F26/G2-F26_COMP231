
package com.example.medclock.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme

@Composable
fun MedicationDetailsScreen(
    medicationName: String,
    dosage: String,
    schedule: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Medication Details",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Medication Name",
            style = MaterialTheme.typography.titleMedium
        )

        Text(text = medicationName)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dosage",
            style = MaterialTheme.typography.titleMedium
        )

        Text(text = dosage)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daily Schedule",
            style = MaterialTheme.typography.titleMedium
        )

        Text(text = schedule)
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationDetailsScreenPreview() {
    MedClockTheme {
        MedicationDetailsScreen(
            medicationName = "Paracetamol",
            dosage = "500 mg",
            schedule = "8:00 AM and 8:00 PM"
        )
    }
}
