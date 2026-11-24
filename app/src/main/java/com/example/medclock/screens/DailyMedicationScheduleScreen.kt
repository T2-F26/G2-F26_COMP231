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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.reminders.ReminderTimeField
import com.example.medclock.ui.theme.MedClockTheme

data class ScheduledDoseInput(
    val time: String = "",
    val dosage: String = "",
    val dosageUnit: String = ""
)


@Composable
fun DailyMedicationScheduleScreen(
    medicationName: String,
    doses: List<ScheduledDoseInput>,
    onDoseChange: (index: Int, dose: ScheduledDoseInput) -> Unit,
    onAddDose: () -> Unit,
    onRemoveDose: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
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
            text = "Daily schedule",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = medicationName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("scheduleMedicationName")
        )
        Text(
            text = "Add each time you take this medication during the day. " +
                    "Reminders and supply estimates use these doses.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (doses.isEmpty()) {
            Text(
                text = "No dose times yet. Tap \"Add dose time\" to start.",
                modifier = Modifier.testTag("scheduleEmpty")
            )
        }

        doses.forEachIndexed { index, dose ->
            ScheduledDoseCard(
                index = index,
                dose = dose,
                onDoseChange = { onDoseChange(index, it) },
                onRemove = { onRemoveDose(index) }
            )
        }

        OutlinedButton(
            onClick = onAddDose,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("addDoseTime")
        ) {
            Text("Add dose time")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("scheduleError")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = actions
        )
    }
}

@Composable
private fun ScheduledDoseCard(
    index: Int,
    dose: ScheduledDoseInput,
    onDoseChange: (ScheduledDoseInput) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Dose ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("removeDose-$index")
                ) {
                    Text("Remove")
                }
            }

            ReminderTimeField(
                currentTime = dose.time,
                onTimeSelected = { onDoseChange(dose.copy(time = it)) },
                label = "Time",
                testTag = "doseTime-$index"
            )

            DosageAndUnitFields(
                dosage = dose.dosage,
                onDosageChange = { onDoseChange(dose.copy(dosage = it)) },
                dosageUnit = dose.dosageUnit,
                onDosageUnitChange = { onDoseChange(dose.copy(dosageUnit = it)) },
                testTagSuffix = "-$index"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyMedicationScheduleScreenPreview() {
    val doses = remember {
        mutableStateListOf(
            ScheduledDoseInput(time = "08:00", dosage = "500", dosageUnit = "mg"),
            ScheduledDoseInput(time = "20:00", dosage = "500", dosageUnit = "mg")
        )
    }

    MedClockTheme {
        DailyMedicationScheduleScreen(
            medicationName = "Metformin",
            doses = doses,
            onDoseChange = { index, dose -> doses[index] = dose },
            onAddDose = { doses.add(ScheduledDoseInput()) },
            onRemoveDose = { doses.removeAt(it) },
            actions = {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Save") }
            }
        )
    }
}
