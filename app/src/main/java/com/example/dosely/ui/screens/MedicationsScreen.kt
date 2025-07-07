package com.example.dosely.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dosely.data.MedicationEntity
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.dosely.data.AppDatabase
import com.example.dosely.data.MedicationRepositoryImpl
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Close
import kotlin.collections.forEachIndexed
import kotlin.collections.toMutableList
import kotlin.collections.joinToString
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.remember

class MedicationsViewModelFactory(private val repository: com.example.dosely.data.MedicationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val owner = LocalViewModelStoreOwner.current
    
    // Color scheme matching dashboard
    val lightBlue = Color(0xFFDAF4FF)
    val mediumBlue = Color(0xFFB0D3E2)
    val darkBlue = Color(0xFF2E7BB8)
    val accentBlue = Color(0xFF4A90E2)
    
    if (owner == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(lightBlue, lightBlue.copy(alpha = 0.7f))
                    )
                ), 
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accentBlue)
        }
        return
    }
    
    // Build Room database and repository
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "dosely-db"
        ).build()
    }
    val repository = remember { MedicationRepositoryImpl(db.medicationDao(), db.doseStatusDao()) }
    val factory = remember { MedicationsViewModelFactory(repository) }
    val viewModel: MedicationsViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = factory
    )
    
    val medications = viewModel.medications.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var recentlyDeleted by remember { mutableStateOf<MedicationEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = accentBlue,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) { 
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Add Medication",
                    modifier = Modifier.size(28.dp)
                ) 
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "My Medications", 
                        fontWeight = FontWeight.Bold,
                        color = darkBlue,
                        fontSize = 22.sp
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = darkBlue
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = lightBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(lightBlue, lightBlue.copy(alpha = 0.7f))
                    )
                )
                .padding(padding)
        ) {
            // Search Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search medications", color = mediumBlue) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = accentBlue
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = mediumBlue.copy(alpha = 0.5f),
                        focusedLabelColor = accentBlue,
                        unfocusedLabelColor = mediumBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            val filteredMedications = medications.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.medicationType.contains(searchQuery, ignoreCase = true)
            }
            
            if (filteredMedications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No medications found.",
                        color = mediumBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredMedications) { med ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon
                                Surface(
                                    color = lightBlue.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            imageVector = when (med.medicationType) {
                                                "Tablet" -> Icons.Filled.MedicalServices
                                                "Capsule" -> Icons.Filled.MedicalServices
                                                "Liquid" -> Icons.Filled.LocalDrink
                                                "Injection" -> Icons.Filled.Vaccines
                                                "Cream" -> Icons.Filled.Healing
                                                "Drops" -> Icons.Filled.Opacity
                                                else -> Icons.Filled.MedicalServices
                                            },
                                            contentDescription = null,
                                            tint = accentBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                // Medication Info
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        med.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = darkBlue,
                                        maxLines = 1
                                    )
                                    Text(
                                        med.dosage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = mediumBlue,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Alarm, contentDescription = null, tint = accentBlue, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            med.times,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = accentBlue,
                                            maxLines = 1
                                        )
                                    }
                                    if (med.notes.isNotBlank()) {
                                        Text(
                                            med.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = mediumBlue,
                                            maxLines = 1
                                        )
                                    }
                                }
                                // Actions
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { editMedication = med; showEditDialog = true }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = darkBlue)
                                    }
                                    IconButton(onClick = {
                                        recentlyDeleted = med
                                        viewModel.deleteMedication(med)
                                        coroutineScope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Medication deleted",
                                                actionLabel = "Undo"
                                            )
                                            if (result == SnackbarResult.ActionPerformed && recentlyDeleted != null) {
                                                viewModel.addMedication(recentlyDeleted!!)
                                                recentlyDeleted = null
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        EnhancedMedicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { med ->
                viewModel.addMedication(med)
                showAddDialog = false
            },
            lightBlue = lightBlue,
            mediumBlue = mediumBlue,
            darkBlue = darkBlue,
            accentBlue = accentBlue
        )
    }
    
    // Edit Dialog
    if (showEditDialog && editMedication != null) {
        EnhancedMedicationDialog(
            initial = editMedication,
            onDismiss = {
                showEditDialog = false
                editMedication = null
            },
            onConfirm = { med ->
                viewModel.updateMedication(med)
                showEditDialog = false
                editMedication = null
            },
            lightBlue = lightBlue,
            mediumBlue = mediumBlue,
            darkBlue = darkBlue,
            accentBlue = accentBlue
        )
    }
}

@Composable
fun EnhancedMedicationCard(
    medication: MedicationEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    lightBlue: Color,
    mediumBlue: Color,
    darkBlue: Color,
    accentBlue: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Medication Icon
                Surface(
                    color = lightBlue.copy(alpha = 0.7f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (medication.medicationType) {
                                "Tablet" -> Icons.Filled.MedicalServices
                                "Capsule" -> Icons.Filled.MedicalServices
                                "Liquid" -> Icons.Filled.LocalDrink
                                "Injection" -> Icons.Filled.Vaccines
                                "Cream" -> Icons.Filled.Healing
                                "Drops" -> Icons.Filled.Opacity
                                else -> Icons.Filled.MedicalServices
                            },
                            contentDescription = null,
                            tint = accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Medication Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )
                    Text(
                        text = medication.dosage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mediumBlue,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Medication Type Badge
                    Surface(
                        color = accentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = medication.medicationType,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Action Buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = accentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Details Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem(
                        label = "Frequency",
                        value = "${medication.frequency}x daily",
                        icon = Icons.Default.Schedule,
                        color = mediumBlue
                    )
                    DetailItem(
                        label = "Duration",
                        value = "${medication.duration} days",
                        icon = Icons.Default.DateRange,
                        color = mediumBlue
                    )
                }
                
                if (medication.times.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = mediumBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Times: ${medication.times}",
                            style = MaterialTheme.typography.bodySmall,
                            color = mediumBlue
                        )
                    }
                }
                
                if (medication.description.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = mediumBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = medication.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = mediumBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMedicationDialog(
    initial: MedicationEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (MedicationEntity) -> Unit,
    lightBlue: Color,
    mediumBlue: Color,
    darkBlue: Color,
    accentBlue: Color
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var dosage by remember { mutableStateOf(initial?.dosage ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var frequency by remember { mutableStateOf(initial?.frequency?.toString() ?: "1") }
    var duration by remember { mutableStateOf(initial?.duration?.toString() ?: "1") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var medicationType by remember { mutableStateOf(initial?.medicationType ?: "Tablet") }
    var timesList by remember { mutableStateOf(
        if (initial?.times?.isNotBlank() == true) initial.times.split(",").map { it.trim() } else mutableListOf<String>()
    ) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val scrollState = rememberScrollState()
    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var dosageError by remember { mutableStateOf<String?>(null) }
    var frequencyError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }
    var timesError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "Add Medication" else "Edit Medication",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Medicine Name") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = { if (nameError != null) Text(nameError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = {
                        dosage = it
                        if (it.isNotBlank()) dosageError = null
                    },
                    label = { Text("Dosage (e.g., 500mg)") },
                    singleLine = true,
                    isError = dosageError != null,
                    supportingText = { if (dosageError != null) Text(dosageError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    singleLine = false
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            frequency = it
                            frequencyError = null
                        } else frequencyError = "Must be a number"
                    },
                    label = { Text("Times per day") },
                    singleLine = true,
                    isError = frequencyError != null,
                    supportingText = { if (frequencyError != null) Text(frequencyError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            duration = it
                            durationError = null
                        } else durationError = "Must be a number"
                    },
                    label = { Text("Days") },
                    singleLine = true,
                    isError = durationError != null,
                    supportingText = { if (durationError != null) Text(durationError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    singleLine = false
                )
                // Medication type selector
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Type:", modifier = Modifier.padding(end = 8.dp))
                    LazyRow {
                        items(listOf("Tablet", "Capsule", "Liquid", "Injection", "Cream", "Drops")) { type ->
                            FilterChip(
                                onClick = { medicationType = type },
                                label = { Text(type) },
                                selected = medicationType == type,
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (type) {
                                            "Tablet" -> Icons.Filled.MedicalServices
                                            "Capsule" -> Icons.Filled.MedicalServices
                                            "Liquid" -> Icons.Filled.LocalDrink
                                            "Injection" -> Icons.Filled.Vaccines
                                            "Cream" -> Icons.Filled.Healing
                                            "Drops" -> Icons.Filled.Opacity
                                            else -> Icons.Filled.MedicalServices
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
                // Time picker section
                Text("Times:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { showTimePicker = true }) {
                        Icon(Icons.Filled.Alarm, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Time")
                    }
                    Spacer(Modifier.width(8.dp))
                    LazyRow(modifier = Modifier.weight(1f)) {
                        items(timesList.size) { idx ->
                            val time = timesList[idx]
                            AssistChip(
                                onClick = {},
                                label = { Text(time) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove time",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                timesList = timesList.toMutableList().also { it.removeAt(idx) }
                                            }
                                    )
                                },
                                shape = RoundedCornerShape(50),
                                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        title = { Text("Pick Time") },
                        text = {
                            TimePicker(state = timePickerState)
                        },
                        confirmButton = {
                            Button(onClick = {
                                val picked = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                val formatted = picked.format(timeFormatter)
                                if (formatted !in timesList) {
                                    timesList = timesList.toMutableList().apply { add(formatted) }
                                    timesError = null
                                }
                                showTimePicker = false
                            }) { Text("Add") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        }
                    )
                }
                if (timesError != null) {
                    Text(timesError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        hasError = true
                    }
                    if (dosage.isBlank()) {
                        dosageError = "Dosage is required"
                        hasError = true
                    }
                    val freqInt = frequency.toIntOrNull()
                    if (freqInt == null || freqInt <= 0) {
                        frequencyError = "Enter a valid number > 0"
                        hasError = true
                    }
                    val durInt = duration.toIntOrNull()
                    if (durInt == null || durInt <= 0) {
                        durationError = "Enter a valid number > 0"
                        hasError = true
                    }
                    if (timesList.isEmpty()) {
                        timesError = "Add at least one time"
                        hasError = true
                    }
                    if (hasError) return@Button
                    onConfirm(
                        MedicationEntity(
                            id = initial?.id ?: 0,
                            name = name,
                            dosage = dosage,
                            description = description,
                            frequency = freqInt!!,
                            times = timesList.joinToString(","),
                            duration = durInt!!,
                            notes = notes,
                            medicationType = medicationType
                        )
                    )
                },
                enabled = true
            ) { Text(if (initial == null) "Add" else "Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}