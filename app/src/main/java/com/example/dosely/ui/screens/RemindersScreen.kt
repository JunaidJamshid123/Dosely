@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.dosely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dosely.data.MedicationEntity
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen() {
    val context = LocalContext.current
    val reminderFactory = remember { RemindersViewModelFactory(context) }
    val remindersViewModel: RemindersViewModel = viewModel(factory = reminderFactory)

    // Collect states from ViewModel
    val medicationsWithReminders by remindersViewModel.medicationsWithReminders.collectAsState()
    val medicationsWithoutReminders by remindersViewModel.medicationsWithoutReminders.collectAsState()

    // Dialog states
    var showEditReminderDialog by remember { mutableStateOf(false) }
    var editMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var selectedMedicationForAdd by remember { mutableStateOf<MedicationEntity?>(null) }

    // Initialize reminders when screen loads
    LaunchedEffect(Unit) {
        remindersViewModel.initializeReminders()
    }

    // Get upcoming reminders (next 3 medications with times)
    val upcomingReminders = remember(medicationsWithReminders) {
        remindersViewModel.getUpcomingReminders()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Medication Reminders", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF2E7BB8),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (medicationsWithoutReminders.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        selectedMedicationForAdd = medicationsWithoutReminders.first()
                        showAddReminderDialog = true
                    },
                    containerColor = Color(0xFF4A90E2),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFDAF4FF))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upcoming Reminders Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A90E2)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Upcoming",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Next Reminders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (upcomingReminders.isEmpty()) {
                            Text(
                                "No upcoming reminders",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        } else {
                            upcomingReminders.forEach { (medication, time) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Medication,
                                            contentDescription = "Medication",
                                            tint = Color(0xFF4A90E2),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                medication.name,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color(0xFF2E7BB8)
                                            )
                                            Text(
                                                time,
                                                fontSize = 12.sp,
                                                color = Color(0xFF666666)
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                remindersViewModel.markMedicationAsTaken(
                                                    medication.id,
                                                    time
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            ),
                                            modifier = Modifier.size(width = 60.dp, height = 32.dp),
                                            contentPadding = PaddingValues(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Mark as Taken",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Medication Reminders Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Medication Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF2E7BB8)
                    )

                    Text(
                        "${medicationsWithReminders.size} active",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            // List of Medications with Reminders
            if (medicationsWithReminders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = "No Reminders",
                                tint = Color(0xFFCCCCCC),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No medication reminders yet",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                "Add medications with reminder times to see them here",
                                fontSize = 14.sp,
                                color = Color(0xFF999999),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(medicationsWithReminders) { medication ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Medication Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Medication,
                                    contentDescription = "Medication",
                                    tint = Color(0xFF4A90E2),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    medication.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF2E7BB8)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Display all reminder times for this medication
                            val times = medication.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            times.forEach { time ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Reminder",
                                        tint = Color(0xFF4A90E2),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        time,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4A90E2),
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = {
                                            editMedication = medication
                                            showEditReminderDialog = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color(0xFF4A90E2)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            remindersViewModel.deleteMedicationReminderTime(medication, time)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFE57373)
                                        )
                                    }
                                }
                            }

                            // Add new time button
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedMedicationForAdd = medication
                                    showAddReminderDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4A90E2)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFF4A90E2)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Time",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Another Time")
                            }
                        }
                    }
                }
            }

            // Medications without reminders section
            if (medicationsWithoutReminders.isNotEmpty()) {
                item {
                    Text(
                        "Medications without Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2E7BB8),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(medicationsWithoutReminders) { medication ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Medication,
                                contentDescription = "Medication",
                                tint = Color(0xFF999999),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                medication.name,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = Color(0xFF2E7BB8),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedButton(
                                onClick = {
                                    selectedMedicationForAdd = medication
                                    showAddReminderDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF4A90E2)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color(0xFF4A90E2)
                                )
                            ) {
                                Text("Add Reminder")
                            }
                        }
                    }
                }
            }
        }

        // Edit Reminder Dialog
        if (showEditReminderDialog && editMedication != null) {
            EditMedicationReminderDialog(
                medication = editMedication!!,
                onDismiss = {
                    showEditReminderDialog = false
                    editMedication = null
                },
                onConfirm = { time ->
                    remindersViewModel.updateMedicationTime(editMedication!!, time)
                    showEditReminderDialog = false
                    editMedication = null
                }
            )
        }

        // Add Reminder Dialog
        if (showAddReminderDialog && selectedMedicationForAdd != null) {
            AddMedicationReminderDialog(
                medication = selectedMedicationForAdd!!,
                onDismiss = {
                    showAddReminderDialog = false
                    selectedMedicationForAdd = null
                },
                onConfirm = { time ->
                    remindersViewModel.addReminderTimeToMedication(selectedMedicationForAdd!!, time)
                    showAddReminderDialog = false
                    selectedMedicationForAdd = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicationReminderDialog(
    medication: MedicationEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = medication.times.let {
            try {
                val firstTime = it.split(",").first().trim()
                LocalTime.parse(firstTime, DateTimeFormatter.ofPattern("hh:mm a")).hour
            } catch (e: Exception) {
                8
            }
        },
        initialMinute = medication.times.let {
            try {
                val firstTime = it.split(",").first().trim()
                LocalTime.parse(firstTime, DateTimeFormatter.ofPattern("hh:mm a")).minute
            } catch (e: Exception) {
                0
            }
        },
        is24Hour = false
    )

    var time by remember {
        mutableStateOf(
            try {
                medication.times.split(",").first().trim()
            } catch (e: Exception) {
                "08:00 AM"
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Reminder Time",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7BB8)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Medication: ${medication.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7BB8)
                )

                // Time Selection
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = { Text("Reminder Time") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        focusedLabelColor = Color(0xFF4A90E2)
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Color(0xFF4A90E2)
                        )
                    }
                )

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        title = {
                            Text(
                                "Select Time",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7BB8)
                            )
                        },
                        text = {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    timeSelectorSelectedContainerColor = Color(0xFF4A90E2),
                                    timeSelectorSelectedContentColor = Color.White
                                )
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val picked = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    val formatted = picked.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                    time = formatted
                                    showTimePicker = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4A90E2)
                                )
                            ) {
                                Text("Set Time")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showTimePicker = false }
                            ) {
                                Text("Cancel", color = Color(0xFF666666))
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(time)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = Color(0xFF666666))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationReminderDialog(
    medication: MedicationEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    var time by remember { mutableStateOf("08:00 AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Reminder Time",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7BB8)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Medication: ${medication.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7BB8)
                )

                // Time Selection
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = { Text("Reminder Time") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        focusedLabelColor = Color(0xFF4A90E2)
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = Color(0xFF4A90E2)
                        )
                    }
                )

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        title = {
                            Text(
                                "Select Time",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7BB8)
                            )
                        },
                        text = {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    timeSelectorSelectedContainerColor = Color(0xFF4A90E2),
                                    timeSelectorSelectedContentColor = Color.White
                                )
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val picked = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    val formatted = picked.format(DateTimeFormatter.ofPattern("hh:mm a"))
                                    time = formatted
                                    showTimePicker = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4A90E2)
                                )
                            ) {
                                Text("Set Time")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showTimePicker = false }
                            ) {
                                Text("Cancel", color = Color(0xFF666666))
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(time)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text("Add Reminder")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = Color(0xFF666666))
            }
        }
    )
}