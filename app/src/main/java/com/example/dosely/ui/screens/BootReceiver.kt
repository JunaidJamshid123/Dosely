package com.example.dosely.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.dosely.data.AppDatabase
import com.example.dosely.data.MedicationRepository
import com.example.dosely.data.MedicationRepositoryImpl

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                rescheduleAllReminders(context)
            }
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        // Use coroutine to handle database operations
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val medicationRepo = MedicationRepositoryImpl(db.medicationDao(), db.doseStatusDao())

                // Create RemindersViewModel to handle rescheduling
                val viewModel = RemindersViewModel(medicationRepo, context.applicationContext)

                // Reschedule all active reminders
                viewModel.scheduleAllActiveReminders()

            } catch (e: Exception) {
                // Log error or handle gracefully
                e.printStackTrace()
            }
        }
    }
}