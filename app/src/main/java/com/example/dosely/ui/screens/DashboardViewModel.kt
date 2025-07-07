package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class for medication entry
data class MedicationEntry(
    val name: String,
    val time: String,
    val status: String // "Taken" or "Pending"
)

class DashboardViewModel : ViewModel() {
    // Mock medication list
    private val _medications = MutableStateFlow(
        listOf(
            MedicationEntry("Paracetamol", "08:00 AM", "Taken"),
            MedicationEntry("Ibuprofen", "12:00 PM", "Pending"),
            MedicationEntry("Vitamin D", "03:00 PM", "Pending"),
            MedicationEntry("Aspirin", "08:00 PM", "Taken")
        )
    )
    val medications: StateFlow<List<MedicationEntry>> = _medications.asStateFlow()

    // Progress: doses taken vs total
    val totalDoses: Int get() = _medications.value.size
    val takenDoses: Int get() = _medications.value.count { it.status == "Taken" }
}
