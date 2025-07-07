package com.example.dosely.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.dosely.R
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch

// Enhanced Data class for Medication
data class Medication(
    val id: Int = 0,
    val name: String,
    val dosage: String,
    val description: String = "",
    val frequency: Int, // times per day
    val times: List<String>, // e.g., ["9:00 AM", "7:00 PM"]
    val duration: Int, // days
    val notes: String = "",
    val medicationType: MedicationType = MedicationType.TABLET,
    val startDate: String = "",
    val endDate: String = ""
)

enum class MedicationType(val displayName: String, val icon: ImageVector) {
    TABLET("Tablet", Icons.Filled.MedicalServices),
    CAPSULE("Capsule", Icons.Filled.MedicalServices),
    LIQUID("Liquid", Icons.Filled.LocalDrink),
    INJECTION("Injection", Icons.Filled.Vaccines),
    CREAM("Cream", Icons.Filled.Healing),
    DROPS("Drops", Icons.Filled.Opacity)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(viewModel: MedicationsViewModel = viewModel()) {
    val medications = viewModel.medications.collectAsState().value
    val showDetailsDialog = viewModel.showDetailsDialog
    val selectedMedication = viewModel.selectedMedication
    val showAddDialog = viewModel.showAddDialog
    val showEditDialog = viewModel.showEditDialog

    val lightBlue = viewModel.lightBlue
    val mediumBlue = viewModel.mediumBlue
    val darkBlue = viewModel.darkBlue
    val accentBlue = viewModel.accentBlue

    val snackbarHostState = rememberSnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var recentlyDeleted by remember { mutableStateOf<MedicationEntity?>(null) }

    Scaffold(
        containerColor = lightBlue,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = accentBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Medication",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Medications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = lightBlue
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lightBlue,
                            lightBlue.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search medications") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                val filteredMedications = medications.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.medicationType.displayName.contains(searchQuery, ignoreCase = true)
                }
                if (filteredMedications.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.MedicalServices ,
                                    contentDescription = null,
                                    tint = mediumBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "No medications yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = darkBlue,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add your first medication to get started with tracking your doses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Summary card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SummaryItem(
                                        title = "Total",
                                        value = "${filteredMedications.size}",
                                        icon = Icons.Filled.MedicalServices,
                                        color = darkBlue
                                    )
                                    SummaryItem(
                                        title = "Active",
                                        value = "${filteredMedications.count { it.duration > 0 }}",
                                        icon = Icons.Default.CheckCircle,
                                        color = accentBlue
                                    )
                                    SummaryItem(
                                        title = "Daily Doses",
                                        value = "${filteredMedications.sumOf { it.frequency }}",
                                        icon = Icons.Default.Schedule,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        // Medications list
                        items(filteredMedications) { medication ->
                            EnhancedMedicationCard(
                                medication = medication,
                                onClick = { viewModel.onMedicationClick(medication) },
                                onEdit = { viewModel.openEditDialog(medication) },
                                onDelete = {
                                    coroutineScope.launch {
                                        recentlyDeleted = medication
                                        viewModel.deleteMedication(medication)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Medication deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed && recentlyDeleted != null) {
                                            viewModel.addMedication(recentlyDeleted!!)
                                            recentlyDeleted = null
                                        }
                                    }
                                },
                                colors = listOf(lightBlue, mediumBlue, darkBlue, accentBlue)
                            )
                        }

                        // Bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showDetailsDialog && selectedMedication != null) {
        EnhancedMedicationDetailsDialog(
            medication = selectedMedication,
            onDismiss = { viewModel.closeDetailsDialog() },
            onEdit = { viewModel.openEditDialog(selectedMedication) },
            onDelete = { viewModel.deleteMedication(selectedMedication) },
            colors = listOf(lightBlue, mediumBlue, darkBlue, accentBlue)
        )
    }

    if (showAddDialog) {
        EnhancedAddMedicationDialog(
            onAdd = { viewModel.addMedication(it) },
            onDismiss = { viewModel.closeAddDialog() },
            colors = listOf(lightBlue, mediumBlue, darkBlue, accentBlue)
        )
    }

    if (showEditDialog && selectedMedication != null) {
        EnhancedEditMedicationDialog(
            medication = selectedMedication,
            onUpdate = { viewModel.updateMedication(it) },
            onDismiss = { viewModel.closeEditDialog() },
            colors = listOf(lightBlue, mediumBlue, darkBlue, accentBlue)
        )
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMedicationCard(
    medication: Medication,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colors: List<Color>
) {
    val (lightBlue, mediumBlue, darkBlue, accentBlue) = colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Medication type icon
                    Surface(
                        color = lightBlue.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = medication.medicationType.icon,
                                contentDescription = null,
                                tint = darkBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = darkBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${medication.dosage} • ${medication.medicationType.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue,
                            fontSize = 13.sp
                        )
                    }
                }

                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = mediumBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency and times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${medication.frequency}x daily",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = Color(0xFFE8F5E8),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${medication.duration} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Times row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(medication.times) { time ->
                    Surface(
                        color = lightBlue.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = darkBlue,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (medication.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = medication.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EnhancedMedicationDetailsDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colors: List<Color>
) {
    val (lightBlue, mediumBlue, darkBlue, accentBlue) = colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = lightBlue.copy(alpha = 0.5f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = medication.medicationType.icon,
                            contentDescription = null,
                            tint = darkBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Dosage", medication.dosage, Icons.Filled.MedicalServices)
                DetailRow("Type", medication.medicationType.displayName, Icons.Filled.MedicalServices)
                DetailRow("Frequency", "${medication.frequency} times per day", Icons.Default.Schedule)

                if (medication.description.isNotBlank()) {
                    DetailRow("Description", medication.description, Icons.Filled.Description)
                }

                // Times
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Times",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = darkBlue
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(medication.times) { time ->
                                Surface(
                                    color = lightBlue.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = darkBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                DetailRow("Duration", "${medication.duration} days", Icons.Filled.Timer)

                if (medication.notes.isNotBlank()) {
                    DetailRow("Notes", medication.notes, Icons.Filled.Notes)
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = accentBlue
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = mediumBlue
                )
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7BB8)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddMedicationDialog(
    onAdd: (Medication) -> Unit,
    onDismiss: () -> Unit,
    colors: List<Color>
) {
    val (lightBlue, mediumBlue, darkBlue, accentBlue) = colors

    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("1") }
    var duration by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MedicationType.TABLET) }
    var selectedTimes by remember { mutableStateOf(listOf<String>()) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = accentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Add Medication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g., 500mg)") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                // Medication type selector
                item {
                    Column {
                        Text(
                            text = "Medication Type",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = darkBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(MedicationType.values()) { type ->
                                FilterChip(
                                    onClick = { selectedType = type },
                                    label = { Text(type.displayName) },
                                    selected = selectedType == type,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = accentBlue
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { if (it.all { char -> char.isDigit() }) frequency = it },
                            label = { Text("Times per day") },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBlue,
                                focusedLabelColor = accentBlue
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                            label = { Text("Days") },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBlue,
                                focusedLabelColor = accentBlue
                            )
                        )
                    }
                }

                // Times selection
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Times (${selectedTimes.size} added)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = darkBlue
                            )
                            TextButton(
                                onClick = { showTimePickerDialog = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = accentBlue
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Time")
                            }
                        }

                        if (selectedTimes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(selectedTimes) { index, time ->
                                    Surface(
                                        color = lightBlue.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = time,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = darkBlue,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.Red,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        selectedTimes =
                                                            selectedTimes.toMutableList().apply {
                                                                removeAt(index)
                                                            }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val freqInt = frequency.toIntOrNull() ?: 1
                    val durInt = duration.toIntOrNull() ?: 1

                    if (name.isNotBlank() && dosage.isNotBlank() && freqInt > 0 && durInt > 0) {
                        onAdd(
                            Medication(
                                name = name,
                                dosage = dosage,
                                description = description,
                                frequency = freqInt,
                                times = selectedTimes,
                                duration = durInt,
                                notes = notes,
                                medicationType = selectedType
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank() &&
                        (frequency.toIntOrNull() ?: 0) > 0 &&
                        (duration.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Medication")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = mediumBlue
                )
            ) {
                Text("Cancel")
            }
        }
    )

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TimePickerDialog(
            onTimeSelected = { time ->
                if (!selectedTimes.contains(time)) {
                    selectedTimes = selectedTimes + time
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEditMedicationDialog(
    medication: Medication,
    onUpdate: (Medication) -> Unit,
    onDismiss: () -> Unit,
    colors: List<Color>
) {
    val (lightBlue, mediumBlue, darkBlue, accentBlue) = colors

    var name by remember { mutableStateOf(medication.name) }
    var dosage by remember { mutableStateOf(medication.dosage) }
    var description by remember { mutableStateOf(medication.description) }
    var frequency by remember { mutableStateOf(medication.frequency.toString()) }
    var duration by remember { mutableStateOf(medication.duration.toString()) }
    var notes by remember { mutableStateOf(medication.notes) }
    var selectedType by remember { mutableStateOf(medication.medicationType) }
    var selectedTimes by remember { mutableStateOf(medication.times) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = accentBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Edit Medication",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                // Similar structure as Add dialog but with pre-filled values
                item {
                    Column {
                        Text(
                            text = "Medication Type",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = darkBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(MedicationType.values()) { type ->
                                FilterChip(
                                    onClick = { selectedType = type },
                                    label = { Text(type.displayName) },
                                    selected = selectedType == type,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = type.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentBlue.copy(alpha = 0.2f),
                                        selectedLabelColor = accentBlue
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { if (it.all { char -> char.isDigit() }) frequency = it },
                            label = { Text("Times per day") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBlue,
                                focusedLabelColor = accentBlue
                            )
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { if (it.all { char -> char.isDigit() }) duration = it },
                            label = { Text("Days") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentBlue,
                                focusedLabelColor = accentBlue
                            )
                        )
                    }
                }

                // Times selection (similar to Add dialog)
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Times (${selectedTimes.size} added)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = darkBlue
                            )
                            TextButton(
                                onClick = { showTimePickerDialog = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = accentBlue
                                )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Time")
                            }
                        }

                        if (selectedTimes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(selectedTimes) { index, time ->
                                    Surface(
                                        color = lightBlue.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = time,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = darkBlue,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.Red,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        selectedTimes =
                                                            selectedTimes.toMutableList().apply {
                                                                removeAt(index)
                                                            }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentBlue,
                            focusedLabelColor = accentBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val freqInt = frequency.toIntOrNull() ?: 1
                    val durInt = duration.toIntOrNull() ?: 1

                    if (name.isNotBlank() && dosage.isNotBlank() && freqInt > 0 && durInt > 0) {
                        onUpdate(
                            medication.copy(
                                name = name,
                                dosage = dosage,
                                description = description,
                                frequency = freqInt,
                                times = selectedTimes,
                                duration = durInt,
                                notes = notes,
                                medicationType = selectedType
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && dosage.isNotBlank() &&
                        (frequency.toIntOrNull() ?: 0) > 0 &&
                        (duration.toIntOrNull() ?: 0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = mediumBlue
                )
            ) {
                Text("Cancel")
            }
        }
    )

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TimePickerDialog(
            onTimeSelected = { time ->
                if (!selectedTimes.contains(time)) {
                    selectedTimes = selectedTimes + time
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFDAF4FF),
                        selectorColor = Color(0xFF4A90E2),
                        containerColor = Color.White,
                        periodSelectorBorderColor = Color(0xFF4A90E2),
                        periodSelectorSelectedContainerColor = Color(0xFF4A90E2),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContainerColor = Color(0xFFDAF4FF),
                        periodSelectorUnselectedContentColor = Color(0xFF2E7BB8),
                        timeSelectorSelectedContainerColor = Color(0xFF4A90E2),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContainerColor = Color(0xFFDAF4FF),
                        timeSelectorUnselectedContentColor = Color(0xFF2E7BB8)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val amPm = if (hour < 12) "AM" else "PM"
                    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                    val formattedTime = String.format("%d:%02d %s", displayHour, minute, amPm)
                    onTimeSelected(formattedTime)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4A90E2)
                )
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFB0D3E2)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}