package com.example.dosely.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.dosely.MainActivity
import com.example.dosely.R
import com.example.dosely.data.AppDatabase
import com.example.dosely.data.MedicationRepositoryImpl
import com.example.dosely.data.DoseStatusEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "medication_reminders"
        private const val NOTIFICATION_CHANNEL_NAME = "Medication Reminders"
        private const val NOTIFICATION_CHANNEL_DESC = "Notifications for medication reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"
        val time = intent.getStringExtra("time") ?: "Now"
        val notificationId = intent.getIntExtra("notificationId", 0)

        createNotificationChannel(context)
        showNotification(context, medicationName, time, notificationId)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = NOTIFICATION_CHANNEL_DESC
                    enableLights(true)
                    enableVibration(true)
                    setBypassDnd(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun showNotification(context: Context, medicationName: String, time: String, notificationId: Int) {
        // Intent to open the app when notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Mark as Taken" action
        val markTakenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_TAKEN
            putExtra("medicationName", medicationName)
            putExtra("time", time)
            putExtra("notificationId", notificationId)
        }

        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Snooze" action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra("medicationName", medicationName)
            putExtra("time", time)
            putExtra("notificationId", notificationId)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.subscription) // Custom vector drawable (see below)
            .setContentTitle("💊 Medication Reminder")
            .setContentText("Time to take $medicationName at $time")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's time to take your $medicationName medication scheduled for $time. Don't forget to take it!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setLights(0xFF4A90E2.toInt(), 1000, 1000)
            .addAction(
                R.drawable.fire, // Custom vector drawable
                "Mark as Taken",
                markTakenPendingIntent
            )
            .addAction(
                R.drawable.clock, // Custom vector drawable
                "Snooze 5 min",
                snoozePendingIntent
            )
            .setDeleteIntent(
                PendingIntent.getBroadcast(
                    context,
                    notificationId * 10 + 3,
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = NotificationActionReceiver.ACTION_DISMISS
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notification)
            }
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
        }
    }
}

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_MARK_TAKEN = "com.example.dosely.ACTION_MARK_TAKEN"
        const val ACTION_SNOOZE = "com.example.dosely.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.example.dosely.ACTION_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"
        val time = intent.getStringExtra("time") ?: "Now"
        val notificationId = intent.getIntExtra("notificationId", 0)
        val medicationId = intent.getIntExtra("medicationId", -1)  // Add this line

        when (intent.action) {
            ACTION_MARK_TAKEN -> {
                markMedicationAsTaken(context, medicationName, time, medicationId)  // Pass medicationId
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(notificationId)
                showConfirmationNotification(context, medicationName, "taken")
            }

            ACTION_SNOOZE -> {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(notificationId)
                scheduleSnoozeReminder(context, medicationName, time, notificationId)
                showConfirmationNotification(context, medicationName, "snoozed for 5 minutes")
            }

            ACTION_DISMISS -> {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(notificationId)
            }
        }
    }

    private fun markMedicationAsTaken(context: Context, medicationName: String, time: String, medicationId: Int) {
        if (medicationId != -1) {
            // Use coroutine to handle database operations
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val medicationRepo = MedicationRepositoryImpl(db.medicationDao(), db.doseStatusDao())
                    
                    // Create a dose status entity and mark it as taken
                    val today = LocalDate.now().toString()
                    val doseStatus = DoseStatusEntity(
                        medicationId = medicationId,
                        time = time,
                        date = today,
                        isTaken = true
                    )
                    medicationRepo.setDoseStatus(doseStatus)
                } catch (e: Exception) {
                    // Log error or handle gracefully
                    e.printStackTrace()
                }
            }
        }
    }

    private fun scheduleSnoozeReminder(context: Context, medicationName: String, time: String, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("medicationName", medicationName)
            putExtra("time", time)
            putExtra("notificationId", notificationId + 1000)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000)

        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            snoozeTime,
            pendingIntent
        )
    }

    private fun showConfirmationNotification(context: Context, medicationName: String, action: String) {
        val confirmationNotification = NotificationCompat.Builder(context, "medication_reminders")
            .setSmallIcon(R.drawable.subscription)
            .setContentTitle("✅ Medication $action")
            .setContentText("$medicationName has been $action")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), confirmationNotification)
            }
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
}