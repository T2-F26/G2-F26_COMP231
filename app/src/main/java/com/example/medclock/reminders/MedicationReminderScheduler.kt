
package com.example.medclock.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.medclock.MainActivity
import com.example.medclock.data.MedClockDemoMedicationStore
import com.example.medclock.data.MedicationReminderStatusStore
import com.example.medclock.data.SavedMedication
import java.time.LocalTime
import java.time.ZonedDateTime

private const val REMINDER_ACTION =
    "com.example.medclock.DOSE_REMINDER"

private const val CHANNEL_ID = "medclock_dose_reminders"

private const val EXTRA_MEDICATION_ID = "medication_id"
private const val EXTRA_DOSE_TIME = "dose_time"

class MedicationReminderScheduler(context: Context) {

    private val appContext = context.applicationContext

    private val alarmManager =
        appContext.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    private val medicationStore =
        MedClockDemoMedicationStore(appContext)

    private val statusStore =
        MedicationReminderStatusStore(appContext)

    // Schedule all daily times for one medication.
    fun scheduleMedication(
        medication: SavedMedication
    ): Boolean {

        cancelMedication(medication)

        val enabled =
            statusStore.getReminderEnabled(medication.id)
                ?: true

        if (!enabled) {
            return true
        }

        return medication.doseTimes.distinct().all { time ->
            scheduleSingleDose(medication.id, time)
        }
    }

    // Cancel future reminders for this medication.
    fun cancelMedication(
        medication: SavedMedication
    ) {
        medication.doseTimes.distinct().forEach { time ->

            val pendingIntent = reminderPendingIntent(
                medicationId = medication.id,
                doseTime = time,
                create = false
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    // Replace the old schedule with the new schedule.
    fun rescheduleMedication(
        oldMedication: SavedMedication,
        updatedMedication: SavedMedication
    ): Boolean {

        cancelMedication(oldMedication)

        return scheduleMedication(updatedMedication)
    }

    // Save reminder ON/OFF and update scheduled alarms.
    fun setReminderEnabled(
        medication: SavedMedication,
        enabled: Boolean
    ): Boolean {

        val saved = statusStore.saveReminderEnabled(
            medicationId = medication.id,
            enabled = enabled
        )

        if (!saved) {
            return false
        }

        return if (enabled) {
            scheduleMedication(medication)
        } else {
            cancelMedication(medication)
            true
        }
    }

    // Delete a medication and cancel its reminders.
    fun deleteMedicationWithReminders(
        medication: SavedMedication
    ): Boolean {

        cancelMedication(medication)

        val deleted =
            medicationStore.deleteMedication(medication.id)

        if (deleted) {
            statusStore.deleteReminderStatus(medication.id)
        } else {
            // Restore reminders if record deletion failed.
            scheduleMedication(medication)
        }

        return deleted
    }

    // Restore reminders after the device restarts.
    fun restoreAllReminders() {

        medicationStore.getMedications().forEach { medication ->
            scheduleMedication(medication)
        }
    }

    // Schedule the next occurrence of a single dose.
    fun scheduleSingleDose(
        medicationId: String,
        doseTime: String
    ): Boolean {

        val parsedTime = runCatching {
            LocalTime.parse(doseTime)
        }.getOrNull() ?: return false

        val now = ZonedDateTime.now()

        var nextDose = now.toLocalDate()
            .atTime(parsedTime)
            .atZone(now.zone)

        if (!nextDose.isAfter(now)) {
            nextDose = nextDose.plusDays(1)
        }

        val pendingIntent = reminderPendingIntent(
            medicationId = medicationId,
            doseTime = doseTime,
            create = true
        ) ?: return false

        return runCatching {

            val triggerTime = nextDose.toInstant()
                .toEpochMilli()

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                // Exact-alarm access is unavailable.
                // Android may deliver this alarm later.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

        }.isSuccess
    }

    private fun reminderPendingIntent(
        medicationId: String,
        doseTime: String,
        create: Boolean
    ): PendingIntent? {

        val intent = Intent(
            appContext,
            MedicationReminderReceiver::class.java
        ).apply {

            action = REMINDER_ACTION

            data = Uri.Builder()
                .scheme("medclock")
                .authority("reminder")
                .appendPath(medicationId)
                .appendPath(doseTime)
                .build()

            putExtra(
                EXTRA_MEDICATION_ID,
                medicationId
            )

            putExtra(
                EXTRA_DOSE_TIME,
                doseTime
            )
        }

        val flags = PendingIntent.FLAG_IMMUTABLE or
                if (create) {
                    PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_NO_CREATE
                }

        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            flags
        )
    }
}

// Receives an alarm and displays its notification.
class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != REMINDER_ACTION) {
            return
        }

        val medicationId =
            intent.getStringExtra(EXTRA_MEDICATION_ID)
                ?: return

        val doseTime =
            intent.getStringExtra(EXTRA_DOSE_TIME)
                ?: return

        val medication =
            MedClockDemoMedicationStore(context)
                .getMedications()
                .firstOrNull {
                    it.id == medicationId
                } ?: return

        val enabled =
            MedicationReminderStatusStore(context)
                .getReminderEnabled(medicationId)
                ?: true

        // Ignore deleted, disabled, or outdated reminders.
        if (!enabled || doseTime !in medication.doseTimes) {
            return
        }

        // Schedule the next daily occurrence.
        MedicationReminderScheduler(context)
            .scheduleSingleDose(medicationId, doseTime)

        // Respect Android notification permission.
        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(
                channel
            )
        }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {
                Notification.Builder(
                    context,
                    CHANNEL_ID
                )
            } else {
                Notification.Builder(context)
            }

        val notification = builder
            .setSmallIcon(
                android.R.drawable.ic_dialog_info
            )
            .setContentTitle("Medication Reminder")
            .setContentText(
                "Time to take ${medication.name} " +
                        "(${medication.dosage})"
            )
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            "${medication.id}_$doseTime",
            1,
            notification
        )
    }
}

// Restores saved alarms after reboot or app update.
class MedicationReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action ==
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {

            MedicationReminderScheduler(context)
                .restoreAllReminders()
        }
    }
}
