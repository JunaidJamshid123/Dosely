package com.example.dosely.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.ReminderEntity
import com.example.dosely.data.ReminderRepository
import com.example.dosely.data.ReminderRepositoryImpl
import com.example.dosely.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RemindersViewModel(
    private val repository: ReminderRepository,
    private val appContext: Context
) : ViewModel() {
    val reminders: StateFlow<List<ReminderEntity>> =
        repository.getAllReminders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addReminder(medicationId: Int, medicationName: String, time: String) {
        viewModelScope.launch {
            val id = repository.insertReminder(ReminderEntity(0, medicationId, medicationName, time, true)).toInt()
            scheduleAlarm(ReminderEntity(id, medicationId, medicationName, time, true))
        }
    }
    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            if (reminder.enabled) scheduleAlarm(reminder) else cancelAlarm(reminder)
        }
    }
    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            cancelAlarm(reminder)
            repository.deleteReminder(reminder)
        }
    }
    fun toggleReminder(reminder: ReminderEntity, enabled: Boolean) {
        updateReminder(reminder.copy(enabled = enabled))
    }
    fun getUpcomingReminder(): ReminderEntity? {
        return reminders.value.filter { it.enabled }
            .minByOrNull { LocalTime.parse(it.time, DateTimeFormatter.ofPattern("hh:mm a")) }
    }
    private fun scheduleAlarm(reminder: ReminderEntity) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra("medicationName", reminder.medicationName)
            putExtra("time", reminder.time)
            putExtra("notificationId", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = Calendar.getInstance().apply {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            val localTime = LocalTime.parse(reminder.time, formatter)
            set(Calendar.HOUR_OF_DAY, localTime.hour)
            set(Calendar.MINUTE, localTime.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }
    private fun cancelAlarm(reminder: ReminderEntity) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(appContext, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class RemindersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getInstance(context)
        val repo = ReminderRepositoryImpl(db.reminderDao())
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(repo, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 