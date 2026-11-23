
package com.example.medclock.schedule

import java.time.LocalDateTime
import java.time.LocalTime

// Represents a medication's saved daily schedule.
data class SavedDoseSchedule(
    val medicationId: String,
    val medicationName: String,
    val doseTimes: List<LocalTime>,
    val remindersEnabled: Boolean = true
)

// Contract for retrieving medication schedules.
interface MedicationScheduleSource {
    fun loadSchedules(): List<SavedDoseSchedule>
}

// Represents one upcoming medication dose.
data class UpcomingDose(
    val medicationId: String,
    val medicationName: String,
    val scheduledAt: LocalDateTime
)

class UpcomingDoseFinder(
    private val source: MedicationScheduleSource
) {

    fun findUpcomingDoses(
        currentTime: LocalDateTime = LocalDateTime.now()
    ): List<UpcomingDose> {

        // Retrieve the schedules from the data source.
        val schedules = source.loadSchedules()

        val upcomingDoses = mutableListOf<UpcomingDose>()

        for (medication in schedules) {

            // Skip medications with disabled reminders.
            if (!medication.remindersEnabled) {
                continue
            }

            for (doseTime in medication.doseTimes.distinct()) {

                // Check the scheduled dose time today.
                var nextDose =
                    currentTime.toLocalDate().atTime(doseTime)

                // If today's time has passed, use tomorrow.
                if (nextDose.isBefore(currentTime)) {
                    nextDose = nextDose.plusDays(1)
                }

                upcomingDoses.add(
                    UpcomingDose(
                        medicationId = medication.medicationId,
                        medicationName = medication.medicationName,
                        scheduledAt = nextDose
                    )
                )
            }
        }

        // Display upcoming doses in chronological order.
        return upcomingDoses.sortedBy {
            it.scheduledAt
        }
    }
}
