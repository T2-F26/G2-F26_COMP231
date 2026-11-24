package com.example.medclock.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.example.medclock.data.MedicationName
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun SavedMedicationNamesList(
    medications: List<MedicationName>,
    onMedicationClick: (MedicationName) -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = {}
) {
    if (medications.isEmpty()) {
        Box(modifier = modifier) {
            emptyContent()
        }
    } else {
        LazyColumn(modifier = modifier.testTag("savedMedicationNames")) {
            items(medications, key = { it.id }) { medication ->
                ListItem(
                    headlineContent = { Text(medication.name) },
                    modifier = Modifier
                        .clickable { onMedicationClick(medication) }
                        .testTag("medicationName-${medication.id}")
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavedMedicationNamesListPreview() {
    MedClockTheme {
        SavedMedicationNamesList(
            medications = listOf(
                MedicationName(id = "1", name = "Ibuprofen"),
                MedicationName(id = "2", name = "Metformin"),
                MedicationName(id = "3", name = "Vitamin D")
            ),
            onMedicationClick = {}
        )
    }
}
