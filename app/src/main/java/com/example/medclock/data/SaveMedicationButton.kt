package com.example.medclock.data

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Must be added to Add Medication form.
 */
@Composable
fun SaveMedicationButton(
    name: String,
    dosageText: String,
    dosageUnit: String,
    onSaved: (Medication) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dosage = dosageText.trim().toDoubleOrNull()
    val canSave = name.isNotBlank() && dosage != null && dosage > 0 && dosageUnit.isNotBlank()

    Button(
        modifier = modifier,
        enabled = canSave,
        onClick = {
            val med = Medication(name = name.trim(), dosage = dosage!!, dosageUnit = dosageUnit.trim())
            MedicationRepository(context).save(med)
            onSaved(med)
        }
    ) { Text("Save") }
}