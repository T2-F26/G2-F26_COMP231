package com.example.medclock.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.medclock.data.Medication
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Schedules local (offline) medication reminders with AlarmManager.
 */
object ReminderScheduler {

    const val CHANNEL_ID = "medclock_medication"
    const val EXTRA_ID = "med_id"
    const val EXTRA_NAME = "med_name"
    const val EXTRA_DOSAGE = "med_dosage"
    const val EXTRA_UNIT = "med_unit"
    const val EXTRA_TIME = "dose_time"

    /** Create the notification channel. Call once from Application/MainActivity onCreate. */
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID, "Medication reminders", NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Reminders to take your medication" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** True if POST_NOTIFICATIONS is granted (always true below Android 13). */
    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    /** Schedules the next occurrence of [time] ("HH:mm") for [med]; the receiver re-arms it daily. */
    fun schedule(context: Context, med: Medication, time: String) {
        val triggerAt = nextTriggerMillis(time)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pi = pendingIntent(context, med, time)

        val canExact = Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun scheduleAll(context: Context, med: Medication, times: List<String>) =
        times.forEach { schedule(context, med, it) }

    fun cancel(context: Context, med: Medication, time: String) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, med, time))
    }

    /**
     * Saves a new reminder time. The old alarm is cancelled first so it never fires again,
     * then the new time is persisted and scheduled. Returns the stored time list.
     */
    fun updateReminderTime(
        context: Context, med: Medication, oldTime: String, newTime: String
    ): List<String> {
        cancel(context, med, oldTime)
        val store = ReminderTimeStore(context)
        val updated = store.get(med.id).filter { it != oldTime }.plus(newTime).distinct().sorted()
        store.save(med.id, updated)
        schedule(context, med, newTime)
        return updated
    }

    internal fun nextTriggerMillis(time: String, now: LocalDateTime = LocalDateTime.now()): Long {
        val t = LocalTime.parse(time)
        var target = LocalDateTime.of(LocalDate.now(), t)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun pendingIntent(context: Context, med: Medication, time: String): PendingIntent {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, med.id)
            putExtra(EXTRA_NAME, med.name)
            putExtra(EXTRA_DOSAGE, med.dosage)
            putExtra(EXTRA_UNIT, med.dosageUnit)
            putExtra(EXTRA_TIME, time)
        }
        return PendingIntent.getBroadcast(
            context, "${med.id}@$time".hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}