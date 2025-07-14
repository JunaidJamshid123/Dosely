package com.example.dosely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sample data classes
data class MedicationRecord(
    val id: Int,
    val medicationName: String,
    val dosage: String,
    val timeTaken: String,
    val dateTaken: String,
    val status: MedicationStatus
)

enum class MedicationStatus {
    TAKEN, MISSED, SKIPPED
}

@Composable
fun HistoryScreen() {
    // Sample data
    val medicationHistory = remember {
        listOf(
            MedicationRecord(1, "Aspirin", "100mg", "8:00 AM", "Today", MedicationStatus.TAKEN),
            MedicationRecord(2, "Vitamin D", "1000 IU", "8:30 AM", "Today", MedicationStatus.TAKEN),
            MedicationRecord(3, "Metformin", "500mg", "6:00 PM", "Yesterday", MedicationStatus.MISSED),
            MedicationRecord(4, "Lisinopril", "10mg", "9:00 AM", "Yesterday", MedicationStatus.TAKEN),
            MedicationRecord(5, "Ibuprofen", "200mg", "2:00 PM", "Yesterday", MedicationStatus.SKIPPED),
            MedicationRecord(6, "Aspirin", "100mg", "8:00 AM", "2 days ago", MedicationStatus.TAKEN),
            MedicationRecord(7, "Vitamin D", "1000 IU", "8:30 AM", "2 days ago", MedicationStatus.TAKEN),
            MedicationRecord(8, "Metformin", "500mg", "6:00 PM", "2 days ago", MedicationStatus.TAKEN),
        )
    }

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Taken", "Missed", "Skipped")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FF))
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF2E3A59)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Stats Cards
        StatsSection()

        Spacer(modifier = Modifier.height(24.dp))

        // Filter Tabs
        FilterSection(
            filters = filters,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Medication History List
        val filteredHistory = when (selectedFilter) {
            "Taken" -> medicationHistory.filter { it.status == MedicationStatus.TAKEN }
            "Missed" -> medicationHistory.filter { it.status == MedicationStatus.MISSED }
            "Skipped" -> medicationHistory.filter { it.status == MedicationStatus.SKIPPED }
            else -> medicationHistory
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group by date
            val groupedHistory = filteredHistory.groupBy { it.dateTaken }
            
            groupedHistory.forEach { (date, records) ->
                item {
                    DateHeader(date = date)
                }
                
                items(records) { record ->
                    HistoryItem(record = record)
                }
            }
        }
    }
}

@Composable
fun StatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatsCard(
            title = "This Week",
            value = "28",
            subtitle = "medications taken",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        StatsCard(
            title = "Streak",
            value = "5",
            subtitle = "days in a row",
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        
        StatsCard(
            title = "Missed",
            value = "2",
            subtitle = "this week",
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF8A8A8A),
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8A8A8A)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FilterSection(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4A90E2),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF2E3A59)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    selectedBorderColor = Color(0xFF4A90E2),
                    borderColor = Color(0xFFE0E0E0),
                    enabled = false,
                    selected = true
                )
            )
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E3A59)
        ),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun HistoryItem(record: MedicationRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to detail */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (record.status) {
                            MedicationStatus.TAKEN -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            MedicationStatus.MISSED -> Color(0xFFFF5722).copy(alpha = 0.1f)
                            MedicationStatus.SKIPPED -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (record.status) {
                        MedicationStatus.TAKEN -> Icons.Default.CheckCircle
                        MedicationStatus.MISSED -> Icons.Default.Cancel
                        MedicationStatus.SKIPPED -> Icons.Default.RemoveCircle
                    },
                    contentDescription = null,
                    tint = when (record.status) {
                        MedicationStatus.TAKEN -> Color(0xFF4CAF50)
                        MedicationStatus.MISSED -> Color(0xFFFF5722)
                        MedicationStatus.SKIPPED -> Color(0xFFFF9800)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Medication info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = record.medicationName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E3A59)
                    )
                )
                
                Text(
                    text = record.dosage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF8A8A8A)
                    )
                )
                
                Text(
                    text = "Scheduled: ${record.timeTaken}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8A8A8A)
                    )
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (record.status) {
                    MedicationStatus.TAKEN -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    MedicationStatus.MISSED -> Color(0xFFFF5722).copy(alpha = 0.1f)
                    MedicationStatus.SKIPPED -> Color(0xFFFF9800).copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = when (record.status) {
                        MedicationStatus.TAKEN -> "Taken"
                        MedicationStatus.MISSED -> "Missed"
                        MedicationStatus.SKIPPED -> "Skipped"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = when (record.status) {
                            MedicationStatus.TAKEN -> Color(0xFF4CAF50)
                            MedicationStatus.MISSED -> Color(0xFFFF5722)
                            MedicationStatus.SKIPPED -> Color(0xFFFF9800)
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}