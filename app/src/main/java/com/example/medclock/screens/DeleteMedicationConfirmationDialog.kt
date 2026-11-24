package com.example.medclock.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun DeleteMedicationConfirmationDialog(
    medicationName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Delete medication?") },
        text = {
            Text(
                "Are you sure you want to delete $medicationName? It will be removed from " +
                        "your medication list and its future reminders will be cancelled."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("confirmDeleteMedication")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancelDeleteMedication")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteMedicationConfirmationDialogPreview() {
    MedClockTheme {
        DeleteMedicationConfirmationDialog(
            medicationName = "Ibuprofen",
            onConfirm = {},
            onCancel = {}
        )
    }
}
