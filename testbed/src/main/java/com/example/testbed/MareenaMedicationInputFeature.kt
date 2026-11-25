
package com.example.medclock.mareena

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Mareena's medication input feature.
 * Related tasks: #117 and #158.
 *
 * Independent of the existing MedClock storage.
 */

data class MareenaMedicationInput(
    val name: String,
    val dosageAmount: String,
    val dosageUnit: String,
    val quantity: String,
    val quantityUnit: String
)

data class MareenaValidatedMedication(
    val name: String,
    val dosage: String,
    val quantity: Double,
    val quantityUnit: String
)

/**
 * Validates medication information before saving.
 */
object MareenaMedicationValidator {

    private val allowedDosageUnits = setOf(
        "mg", "mcg", "g", "ml", "iu"
    )

    private val allowedQuantityUnits = setOf(
        "tablets", "capsules", "ml"
    )

    fun validate(
        input: MareenaMedicationInput
    ): List<String> {

        val errors = mutableListOf<String>()

        val name = input.name.trim()
        val dosage = input.dosageAmount.trim()
            .toDoubleOrNull()
        val quantity = input.quantity.trim()
            .toDoubleOrNull()

        if (name.isBlank()) {
            errors.add("Medication name is required.")
        } else if (name.length > 100) {
            errors.add("Medication name is too long.")
        }

        if (
            dosage == null ||
            !dosage.isFinite() ||
            dosage <= 0.0
        ) {
            errors.add(
                "Enter a positive dosage amount."
            )
        }

        if (
            input.dosageUnit.trim().lowercase()
            !in allowedDosageUnits
        ) {
            errors.add(
                "Dosage unit must be mg, mcg, g, mL, or IU."
            )
        }

        if (
            quantity == null ||
            !quantity.isFinite() ||
            quantity < 0.0
        ) {
            errors.add(
                "Enter a valid non-negative quantity."
            )
        }

        if (
            input.quantityUnit.trim().lowercase()
            !in allowedQuantityUnits
        ) {
            errors.add(
                "Quantity unit must be tablets, capsules, or mL."
            )
        }

        return errors
    }

    fun prepareForSaving(
        input: MareenaMedicationInput
    ): MareenaValidatedMedication? {

        if (validate(input).isNotEmpty()) {
            return null
        }

        val dosage = input.dosageAmount.trim()
            .toDouble()

        val quantity = input.quantity.trim()
            .toDouble()

        val dosageUnit =
            input.dosageUnit.trim().lowercase()

        val supplyUnit =
            input.quantityUnit.trim().lowercase()

        return MareenaValidatedMedication(
            name = input.name.trim(),
            dosage = "${dosage.toCleanString()} " +
                    when (dosageUnit) {
                        "ml" -> "mL"
                        "iu" -> "IU"
                        else -> dosageUnit
                    },
            quantity = quantity,
            quantityUnit = if (supplyUnit == "ml") {
                "mL"
            } else {
                supplyUnit
            }
        )
    }

    private fun Double.toCleanString(): String {
        return java.math.BigDecimal.valueOf(this)
            .stripTrailingZeros()
            .toPlainString()
    }
}

/**
 * Standalone medication form.
 *
 * The parent screen supplies the save operation.
 * No existing MedClock records are modified here.
 */
@Composable
fun MareenaMedicationInputScreen(
    onSave: (MareenaValidatedMedication) -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {

    var medicationName by rememberSaveable {
        mutableStateOf("")
    }

    var dosageAmount by rememberSaveable {
        mutableStateOf("")
    }

    var dosageUnit by rememberSaveable {
        mutableStateOf("mg")
    }

    var quantity by rememberSaveable {
        mutableStateOf("")
    }

    var quantityUnit by rememberSaveable {
        mutableStateOf("tablets")
    }

    var errors by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var savedMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Text(
            "Add Medication",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            "Enter the medication information below."
        )

        HorizontalDivider()

        OutlinedTextField(
            value = medicationName,
            onValueChange = {
                medicationName = it
                errors = emptyList()
                savedMessage = null
            },
            label = {
                Text("Medication Name *")
            },
            placeholder = {
                Text("Example: Paracetamol")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dosageAmount,
            onValueChange = {
                dosageAmount = it
                errors = emptyList()
                savedMessage = null
            },
            label = {
                Text("Dosage Amount *")
            },
            placeholder = {
                Text("Example: 500")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dosageUnit,
            onValueChange = {
                dosageUnit = it
                errors = emptyList()
                savedMessage = null
            },
            label = {
                Text("Dosage Unit *")
            },
            supportingText = {
                Text("mg, mcg, g, mL, or IU")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        Text(
            "Medication Supply",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = quantity,
            onValueChange = {
                quantity = it
                errors = emptyList()
                savedMessage = null
            },
            label = {
                Text("Quantity *")
            },
            placeholder = {
                Text("Example: 30")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = quantityUnit,
            onValueChange = {
                quantityUnit = it
                errors = emptyList()
                savedMessage = null
            },
            label = {
                Text("Quantity Unit *")
            },
            supportingText = {
                Text("tablets, capsules, or mL")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text(
            "* Required fields",
            style = MaterialTheme.typography.bodySmall
        )

        errors.forEach { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        savedMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {

                val input = MareenaMedicationInput(
                    name = medicationName,
                    dosageAmount = dosageAmount,
                    dosageUnit = dosageUnit,
                    quantity = quantity,
                    quantityUnit = quantityUnit
                )

                errors = MareenaMedicationValidator
                    .validate(input)

                if (errors.isEmpty()) {

                    val prepared =
                        MareenaMedicationValidator
                            .prepareForSaving(input)

                    if (
                        prepared != null &&
                        onSave(prepared)
                    ) {
                        savedMessage =
                            "Medication information saved."
                    } else {
                        errors = listOf(
                            "Unable to save medication."
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Medication")
        }

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}

/**
 * Preview demonstrates the form.
 * It does not save to existing medication storage.
 */
@Preview(showBackground = true)
@Composable
private fun MareenaMedicationInputPreview() {

    MaterialTheme {
        MareenaMedicationInputScreen(
            onSave = { true },
            onCancel = {}
        )
    }
}
