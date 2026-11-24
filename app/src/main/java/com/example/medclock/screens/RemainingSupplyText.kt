package com.example.medclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.supply.formatSupplyQuantity
import com.example.medclock.ui.theme.MedClockTheme

@Composable
fun RemainingSupplyText(
    remainingQuantity: Double?,
    unit: String,
    modifier: Modifier = Modifier,
    notRecordedText: String = "Not recorded"
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Estimated remaining supply",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = if (remainingQuantity == null) {
                notRecordedText
            } else {
                "${formatSupplyQuantity(remainingQuantity)} $unit".trim()
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("remainingSupplyValue")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RemainingSupplyTextPreview() {
    MedClockTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RemainingSupplyText(remainingQuantity = 24.0, unit = "tablets")
            RemainingSupplyText(remainingQuantity = null, unit = "tablets")
        }
    }
}
