
package com.example.medclock.mareena

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Separate testbed for Mareena's Iteration 1 tasks.
 * Does not replace MedClockHome or MainActivity.
 */
class MareenaTestbedActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MareenaTestbedScreen()
            }
        }
    }
}

@Composable
private fun MareenaTestbedScreen() {

    val context = LocalContext.current

    // Separate storage for testing the medication form.
    val testPreferences = remember(context) {
        context.getSharedPreferences(
            "mareena_medication_form_testbed",
            Context.MODE_PRIVATE
        )
    }

    var page by rememberSaveable {
        mutableStateOf("menu")
    }

    var savedSummary by remember {
        mutableStateOf(
            testPreferences.getString(
                "saved_medication",
                "No test medication saved yet."
            ) ?: "No test medication saved yet."
        )
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (page != "menu") {

            TextButton(
                onClick = {
                    page = "menu"
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Back to Mareena Testbed")
            }
        }

        when (page) {

            "menu" -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        "Mareena Feature Testbed",
                        style =
                            MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        "Test medication inputs, validation, " +
                                "and reminder settings."
                    )

                    HorizontalDivider()

                    Button(
                        onClick = {
                            page = "medication"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Medication Input Form")
                    }

                    Button(
                        onClick = {
                            page = "reminders"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Medication Reminders")
                    }

                    HorizontalDivider()

                    Text(
                        "Last Saved Test Medication",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(savedSummary)

                    Text(
                        "The input form uses separate test " +
                                "storage. Reminder controls use " +
                                "existing MedClock medication records.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.S
                    ) {

                        OutlinedButton(
                            onClick = {

                                val alarmManager =
                                    context.getSystemService(
                                        Context.ALARM_SERVICE
                                    ) as AlarmManager

                                if (
                                    alarmManager.canScheduleExactAlarms()
                                ) {

                                    message =
                                        "Exact alarm access is enabled."

                                } else {

                                    val intent = Intent(
                                        Settings
                                            .ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        Uri.parse(
                                            "package:${context.packageName}"
                                        )
                                    )

                                    runCatching {
                                        context.startActivity(intent)
                                    }.onFailure {
                                        message =
                                            "Open Android settings to " +
                                                    "allow alarms for MedClock."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Exact Alarm Settings")
                        }
                    }

                    message?.let {
                        Text(it)
                    }
                }
            }

            "medication" -> {

                MareenaMedicationInputScreen(
                    onSave = { medication ->

                        val summary =
                            "${medication.name} | " +
                                    "${medication.dosage} | " +
                                    "${medication.quantity} " +
                                    medication.quantityUnit

                        val saved =
                            testPreferences.edit()
                                .putString(
                                    "saved_medication",
                                    summary
                                )
                                .commit()

                        if (saved) {
                            savedSummary = summary
                        }

                        saved
                    },
                    onCancel = {
                        page = "menu"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            "reminders" -> {

                MareenaMedicationReminderScreen(
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
