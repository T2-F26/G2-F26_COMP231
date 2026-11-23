
package com.example.medclock.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.data.SavedMedication
import com.example.medclock.ui.theme.MedClockTheme

@Composable
fun MedicationListScreen(
    medications: List<SavedMedication>,
    onMedicationSelected: (SavedMedication) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Medications",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (medications.isEmpty()) {
            Text("No medications saved yet.")
        } else {
            LazyColumn {
                items(
                    items = medications,
                    key = { it.id }
                ) { medication ->
                    Text(
                        text = medication.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMedicationSelected(medication)
                            }
                            .padding(vertical = 16.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationListScreenPreview() {
    MedClockTheme {
        MedicationListScreen(
            medications = listOf(
                SavedMedication(
                    id = "1",
                    name = "Paracetamol",
                    dosage = "500 mg"
                ),
                SavedMedication(
                    id = "2",
                    name = "Vitamin D",
                    dosage = "1000 IU"
                )
            ),
            onMedicationSelected = {}
        )
    }
}
