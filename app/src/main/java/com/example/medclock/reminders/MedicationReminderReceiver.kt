package com.example.medclock.reminders

import android.R
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.medclock.MainActivity
import com.example.medclock.data.Medication
import android.app.PendingIntent

/**
 * Builds the notification showing medication, dosage and dosage unit,
 * then re-arms the same dose time for the next day.
 */
class MedicationReminderReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(ReminderScheduler.EXTRA_ID) ?: return
        val name = intent.getStringExtra(ReminderScheduler.EXTRA_NAME) ?: return
        val dosage = intent.getDoubleExtra(ReminderScheduler.EXTRA_DOSAGE, 0.0)
        val unit = intent.getStringExtra(ReminderScheduler.EXTRA_UNIT) ?: ""
        val time = intent.getStringExtra(ReminderScheduler.EXTRA_TIME) ?: return

        if (ReminderScheduler.hasNotificationPermission(context)) {
            ReminderScheduler.createChannel(context)
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_popup_reminder)
                .setContentTitle("Time to take $name")
                .setContentText("Dose: ${formatDosage(dosage)} $unit".trim())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            context.getSystemService(NotificationManager::class.java)
                .notify("$id@$time".hashCode(), notification)
        }

        // Re-arm for tomorrow (a disabled/deleted medication should call ReminderScheduler.cancel).
        ReminderScheduler.schedule(context, Medication(id, name, dosage, unit), time)
    }

    private fun formatDosage(d: Double) = if (d % 1.0 == 0.0) d.toInt().toString() else d.toString()
}