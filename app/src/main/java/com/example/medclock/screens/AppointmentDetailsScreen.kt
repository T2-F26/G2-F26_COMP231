package com.example.medclock.screens

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medclock.appointments.displayAppointmentDate
import com.example.medclock.reminders.displayClockTime
import com.example.medclock.ui.theme.MedClockTheme


@Composable
fun AppointmentDetailsScreen(
    doctorName: String,
    date: String,
    time: String,
    location: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.testTag("appointmentDetailsBack")
        ) {
            Text("Back")
        }

        Text(
            text = "Upcoming appointment",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppointmentDetailRow("Doctor", doctorName, "appointmentDoctor")
                AppointmentDetailRow("Date", displayAppointmentDate(date), "appointmentDate")
                AppointmentDetailRow("Time", displayClockTime(time, is24Hour), "appointmentTime")
                AppointmentDetailRow("Location", location, "appointmentLocation")
            }
        }
    }
}

@Composable
private fun AppointmentDetailRow(label: String, value: String, testTag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value.ifBlank { "Not provided" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppointmentDetailsScreenPreview() {
    MedClockTheme {
        AppointmentDetailsScreen(
            doctorName = "Dr. Maria Lopez",
            date = "2026-12-01",
            time = "14:30",
            location = "Downtown Family Clinic, Room 204",
            onBack = {}
        )
    }
}
