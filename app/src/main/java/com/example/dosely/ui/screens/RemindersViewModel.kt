package com.example.dosely.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.AppDatabase
import com.example.dosely.data.MedicationRepository
import com.example.dosely.data.MedicationRepositoryImpl
import com.example.dosely.data.MedicationEntity
import com.example.dosely.data.DoseStatusEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RemindersViewModel(
    private val medicationRepository: MedicationRepository,
    private val appContext: Context
) : ViewModel() {

    // Fetch all medications
    val medications: StateFlow<List<MedicationEntity>> =
        medicationRepository.getAllMedications().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Get medications that have reminder times set
    val medicationsWithReminders: StateFlow<List<MedicationEntity>> =
        medications.map { medicationList ->
            medicationList.filter { medication ->
                medication.times.isNotBlank() && medication.times.split(",").any { it.trim().isNotBlank() }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Get medications without reminder times
    val medicationsWithoutReminders: StateFlow<List<MedicationEntity>> =
        medications.map { medicationList ->
            medicationList.filter { medication ->
                medication.times.isBlank() || medication.times.split(",").all { it.trim().isBlank() }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Get upcoming reminders for the next 3 medications
    fun getUpcomingReminders(): List<Pair<MedicationEntity, String>> {
        val now = LocalTime.now()
        val upcomingList = mutableListOf<Pair<MedicationEntity, String>>()

        medicationsWithReminders.value.forEach { medication ->
            val times = medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
            times.forEach { time ->
                try {
                    val reminderTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a"))
                    val adjustedTime = if (reminderTime.isBefore(now)) {
                        reminderTime.plusHours(24) // Consider for tomorrow
                    } else {
                        reminderTime
                    }
                    upcomingList.add(Pair(medication, time))
                } catch (e: Exception) {
                    // Skip invalid times
                }
            }
        }

        return upcomingList.sortedBy { (_, time) ->
            try {
                val reminderTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a"))
                if (reminderTime.isBefore(now)) {
                    reminderTime.plusHours(24)
                } else {
                    reminderTime
                }
            } catch (e: Exception) {
                LocalTime.MAX
            }
        }.take(3)
    }

    fun markMedicationAsTaken(medicationId: Int, time: String) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val doseStatus = DoseStatusEntity(
                medicationId = medicationId,
                time = time,
                date = today,
                isTaken = true
            )
            medicationRepository.setDoseStatus(doseStatus)
        }
    }

    fun updateMedicationTime(medication: MedicationEntity, newTime: String) {
        viewModelScope.launch {
            // Cancel existing alarms first
            cancelAlarmForMedication(medication)

            // Update medication time
            val updatedMedication = medication.copy(times = newTime)
            medicationRepository.updateMedication(updatedMedication)

            // Schedule new alarm if time is not blank
            if (newTime.isNotBlank()) {
                scheduleAlarmForMedication(updatedMedication)
            }
        }
    }

    fun deleteMedicationReminderTime(medication: MedicationEntity, timeToDelete: String) {
        viewModelScope.launch {
            // Cancel alarm for this specific time
            cancelAlarmForMedicationTime(medication, timeToDelete)

            // Remove this specific time from the medication's times
            val timesList = medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val newTimes = timesList.filter { it != timeToDelete }.joinToString(", ")

            val updatedMedication = medication.copy(times = newTimes)
            medicationRepository.updateMedication(updatedMedication)
        }
    }

    fun addReminderTimeToMedication(medication: MedicationEntity, newTime: String) {
        viewModelScope.launch {
            val existingTimes = if (medication.times.isBlank()) {
                emptyList()
            } else {
                medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }

            // Add new time if it doesn't already exist
            if (!existingTimes.contains(newTime)) {
                val updatedTimes = (existingTimes + newTime).joinToString(", ")
                val updatedMedication = medication.copy(times = updatedTimes)
                medicationRepository.updateMedication(updatedMedication)

                // Schedule alarm for the new time
                scheduleAlarmForMedicationTime(updatedMedication, newTime)
            }
        }
    }

    fun scheduleAllActiveReminders() {
        viewModelScope.launch {
            medicationsWithReminders.value.forEach { medication ->
                scheduleAlarmForMedication(medication)
            }
        }
    }

    fun cancelAllReminders() {
        viewModelScope.launch {
            medications.value.forEach { medication ->
                cancelAlarmForMedication(medication)
            }
        }
    }

    private fun scheduleAlarmForMedication(medication: MedicationEntity) {
        if (medication.times.isBlank()) return

        val times = medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
        times.forEach { time ->
            scheduleAlarmForMedicationTime(medication, time)
        }
    }

    private fun scheduleAlarmForMedicationTime(medication: MedicationEntity, time: String) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create a unique request code for this medication and time combination
        val requestCode = "${medication.id}_${time}".hashCode()

        // Use ReminderReceiver instead of MedicationReminderReceiver
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra("medicationName", medication.name)
            putExtra("time", time)
            putExtra("notificationId", requestCode)
            putExtra("medicationId", medication.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            try {
                val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                val localTime = LocalTime.parse(time, formatter)
                set(Calendar.HOUR_OF_DAY, localTime.hour)
                set(Calendar.MINUTE, localTime.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time has passed today, schedule for tomorrow
                if (before(Calendar.getInstance())) {
                    add(Calendar.DATE, 1)
                }
            } catch (e: Exception) {
                // If time parsing fails, set to 8:00 AM tomorrow
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DATE, 1)
            }
        }

        try {
            // Use repeating alarm for daily reminders
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle the case where SCHEDULE_EXACT_ALARM permission is not granted
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } catch (e2: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun cancelAlarmForMedication(medication: MedicationEntity) {
        if (medication.times.isBlank()) return

        val times = medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
        times.forEach { time ->
            cancelAlarmForMedicationTime(medication, time)
        }
    }

    private fun cancelAlarmForMedicationTime(medication: MedicationEntity, time: String) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = "${medication.id}_${time}".hashCode()

        // Use ReminderReceiver instead of MedicationReminderReceiver
        val intent = Intent(appContext, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // Call this when the app starts to ensure all reminders are properly scheduled
    fun initializeReminders() {
        viewModelScope.launch {
            scheduleAllActiveReminders()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
    }
}

// Remove this placeholder class
// class MedicationReminderReceiver : android.content.BroadcastReceiver() {
//     override fun onReceive(context: Context?, intent: Intent?) {
//         // Handle the alarm trigger here
//         // Show notification, etc.
//     }
// }

class RemindersViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(context)
        val medicationRepo = MedicationRepositoryImpl(
            db.medicationDao(),
            db.doseStatusDao()
        )

        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(medicationRepo, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}