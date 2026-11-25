package com.example.medclock.schedule

import com.example.medclock.data.MedClockDemoMedicationStore
import com.example.medclock.data.MedicationReminderStatusStore
import java.time.LocalTime

class DemoMedicationScheduleSource(
    private val medicationStore: MedClockDemoMedicationStore,
    private val reminderStore: MedicationReminderStatusStore
) : MedicationScheduleSource {

    override fun loadSchedules(): List<SavedDoseSchedule> {
        return medicationStore.getMedications().mapNotNull { medication ->

            val times = medication.doseTimes.mapNotNull { time ->
                runCatching {
                    LocalTime.parse(time)
                }.getOrNull()
            }

            if (times.isEmpty()) {
                null
            } else {
                SavedDoseSchedule(
                    medicationId = medication.id,
                    medicationName = medication.name,
                    doseTimes = times,
                    remindersEnabled =
                        reminderStore.getReminderEnabled(
                            medication.id
                        ) ?: true
                )
            }
        }
    }
}
