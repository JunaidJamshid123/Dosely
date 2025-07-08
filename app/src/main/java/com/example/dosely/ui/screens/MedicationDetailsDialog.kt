package com.example.dosely.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import com.example.dosely.data.MedicationEntity
import androidx.compose.ui.unit.dp

// Dialog for MedicationEntity (MedicationsScreen)
@Composable
fun MedicationEntityDetailsDialog(medication: MedicationEntity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(medication.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dosage: ${medication.dosage}")
                Text("Type: ${medication.medicationType}")
                if (medication.times.isNotBlank()) Text("Times: ${medication.times}")
                if (medication.notes.isNotBlank()) Text("Notes: ${medication.notes}")
                if (medication.description.isNotBlank()) Text("Description: ${medication.description}")
                Text("Frequency: ${medication.frequency}x daily")
                Text("Duration: ${medication.duration} days")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// Dialog for MedicationEntry (DashboardScreen)
@Composable
fun MedicationEntryDetailsDialog(medication: MedicationEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(medication.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Time: ${medication.time}")
                Text("Status: ${medication.status}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
} 