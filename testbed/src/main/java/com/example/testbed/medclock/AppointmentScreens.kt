package com.example.testbed.medclock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AppointmentListScreen(appointments: List<Appointment>, onBack: () -> Unit) {
    val orderedAppointments = remember(appointments) { appointmentsInDateOrder(appointments) }
    val dateFormat = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).testTag("appointmentList"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text("Appointments", style = MaterialTheme.typography.headlineSmall)
        if (orderedAppointments.isEmpty()) {
            Text("No saved appointments.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orderedAppointments, key = { it.id }) { appointment ->
                    Card(modifier = Modifier.fillMaxWidth().testTag("appointment-${appointment.id}")) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(appointment.doctorName, style = MaterialTheme.typography.titleMedium)
                            Text("Date: ${LocalDate.parse(appointment.date).format(dateFormat)}")
                            Text("Time: ${appointment.time}")
                            Text("Location: ${appointment.location}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentDraftScreen(onCancel: () -> Unit) {
    var doctorName by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    TaskPage("New appointment draft", onCancel) {
        OutlinedTextField(
            value = doctorName, onValueChange = { doctorName = it },
            label = { Text("Doctor's name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appointmentDoctorInput")
        )
        OutlinedTextField(
            value = date, onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appointmentDateInput")
        )
        OutlinedTextField(
            value = time, onValueChange = { time = it },
            label = { Text("Time (HH:mm)") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appointmentTimeInput")
        )
        OutlinedTextField(
            value = location, onValueChange = { location = it },
            label = { Text("Location") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("appointmentLocationInput")
        )
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            label = { Text("Notes") }, minLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("appointmentNotesInput")
        )
        Text("Cancel returns to the appointment list without saving this draft.")
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().testTag("cancelAppointment")) {
            Text("Cancel")
        }
    }
}
