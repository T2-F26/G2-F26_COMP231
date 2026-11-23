package com.example.medclock.reminders

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** How long before an appointment the user wants to be reminded. */
enum class ReminderTiming(val label: String, val minutesBefore: Long) {
    FIFTEEN_MIN("15 minutes before", 15),
    THIRTY_MIN("30 minutes before", 30),
    ONE_HOUR("1 hour before", 60),
    ONE_DAY("1 day before", 24 * 60),
    TWO_DAYS("2 days before", 2 * 24 * 60)
}

/** Lets the user choose when to be reminded. Save/Cancel buttons belong to the surrounding form. */
@Composable
fun ReminderTimingSelector(
    selected: ReminderTiming,
    onSelected: (ReminderTiming) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        ReminderTiming.entries.forEach { option ->
            Row(
                Modifier
                    .selectable(
                        selected = option == selected,
                        onClick = { onSelected(option) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(selected = option == selected, onClick = null)
                Text(option.label, Modifier.padding(start = 12.dp))
            }
        }
    }
}