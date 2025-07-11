package com.example.dosely.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

// Reminder data model
data class Reminder(
    val id: Int,
    val medicationName: String,
    val time: String, // e.g., "08:00 AM"
    val enabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen() {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }
    var nextId = 1
    // appContext removed

    var showDialog by remember { mutableStateOf(false) }
    var editReminder by remember { mutableStateOf<Reminder?>(null) }
    val upcoming = reminders.filter { it.enabled }
        .minByOrNull { LocalTime.parse(it.time, DateTimeFormatter.ofPattern("hh:mm a")) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.Bold, fontSize = 22.sp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDAF4FF))
                .padding(padding)
        ) {
            // Upcoming Reminder Card
            if (upcoming != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Next Reminder:", fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7BB8))
                        Text(
                            "${upcoming.medicationName} at ${upcoming.time}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF4A90E2)
                        )
                    }
                }
            }
            // List of Reminders
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reminders) { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(reminder.medicationName, fontWeight = FontWeight.Bold, color = Color(0xFF2E7BB8))
                                Text(reminder.time, color = Color(0xFF4A90E2))
                            }
                            Switch(
                                checked = reminder.enabled,
                                onCheckedChange = { isChecked ->
                                    reminders = reminders.map { if (it.id == reminder.id) it.copy(enabled = isChecked) else it }
                                    reminders.find { it.id == reminder.id }?.let {
                                        if (isChecked) scheduleAlarm(context, it) else cancelAlarm(context, it)
                                    }
                                }
                            )
                            IconButton(onClick = { editReminder = reminder; showDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                reminders = reminders.filter { it.id != reminder.id }
                                cancelAlarm(context, reminder)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        // Add/Edit Reminder Dialog
        if (showDialog) {
            ReminderDialog(
                initial = editReminder,
                onDismiss = { showDialog = false; editReminder = null },
                onConfirm = { name, time ->
                    if (editReminder == null) {
                        val reminder = Reminder(nextId++, name, time, true)
                        reminders = reminders + reminder
                        scheduleAlarm(context, reminder)
                    } else {
                        reminders = reminders.map { if (it.id == editReminder!!.id) editReminder!!.copy(medicationName = name, time = time) else it }
                        reminders.find { it.id == editReminder!!.id }?.let {
                            if (it.enabled) scheduleAlarm(context, it) else cancelAlarm(context, it)
                        }
                    }
                    showDialog = false; editReminder = null
                }
            )
        }
    }
}

@Composable
fun ReminderDialog(initial: Reminder?, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf(initial?.medicationName ?: "") }
    var time by remember { mutableStateOf(initial?.time ?: "08:00 AM") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Reminder" else "Edit Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medication Name") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g., 08:00 AM)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, time) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun scheduleAlarm(context: Context, reminder: Reminder) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("medicationName", reminder.medicationName)
        putExtra("time", reminder.time)
        putExtra("notificationId", reminder.id)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
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

private fun cancelAlarm(context: Context, reminder: Reminder) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
} 