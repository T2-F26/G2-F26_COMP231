package com.example.medclock.reminders

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.ui.theme.MedClockTheme
import java.time.LocalTime

private val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(8, 0)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select reminder time"
) {
    val initial = parseClockTime(initialTime) ?: DEFAULT_REMINDER_TIME
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(
                onClick = { onTimeSelected(formatClockTime(pickerState.hour, pickerState.minute)) },
                modifier = Modifier.testTag("confirmReminderTime")
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismissReminderTime")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReminderTimeField(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Reminder time",
    testTag: String = "reminderTimeField"
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    var showPicker by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = if (currentTime.isBlank()) "Not set" else displayClockTime(currentTime, is24Hour),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("$testTag-value")
            )
        }

        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.testTag("$testTag-change")
        ) {
            Text(if (currentTime.isBlank()) "Choose time" else "Change time")
        }
    }

    if (showPicker) {
        ReminderTimePickerDialog(
            initialTime = currentTime,
            onTimeSelected = { time ->
                onTimeSelected(time)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTimeFieldPreview() {
    MedClockTheme {
        ReminderTimeField(
            currentTime = "08:30",
            onTimeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
