package com.example.medclock.screens

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.example.medclock.reminders.DraftValue
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun <T> ReminderTimingCancelButton(
    timing: DraftValue<T>,
    onCancel: (DraftValue<T>) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onCancel(timing.discard()) },
        modifier = modifier.testTag("cancelReminderTiming")
    ) {
        Text("Cancel")
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTimingCancelButtonPreview() {
    MedClockTheme {
        ReminderTimingCancelButton(
            timing = DraftValue(saved = "1 day before", draft = "1 hour before"),
            onCancel = {}
        )
    }
}
