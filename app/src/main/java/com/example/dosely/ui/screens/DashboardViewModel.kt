package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.MedicationEntity
import com.example.dosely.data.MedicationRepository
import com.example.dosely.data.DoseStatusEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

// Data class for medication entry
data class MedicationEntry(
    val name: String,
    val time: String,
    val status: String, // "Taken" or "Pending"
    val medicationId: Int,
    val timeRaw: String
)

class DashboardViewModel(
    private val repository: MedicationRepository
) : ViewModel() {
    private val today: String = LocalDate.now().toString()
    // Combine medications and today's dose statuses
    val medications: StateFlow<List<MedicationEntry>> =
        combine(
            repository.getAllMedications(),
            repository.getAllDoseStatusForDate(today)
        ) { meds, statuses ->
            meds.flatMap { med ->
                val times = med.times.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (times.isEmpty()) {
                    val status = statuses.find { it.medicationId == med.id && it.time == "--" }?.isTaken == true
                    listOf(
                        MedicationEntry(
                            name = med.name,
                            time = "--",
                            status = if (status) "Taken" else "Pending",
                            medicationId = med.id,
                            timeRaw = "--"
                        )
                    )
                } else {
                    times.map { time ->
                        val status = statuses.find { it.medicationId == med.id && it.time == time }?.isTaken == true
                        MedicationEntry(
                            name = med.name,
                            time = time,
                            status = if (status) "Taken" else "Pending",
                            medicationId = med.id,
                            timeRaw = time
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val takenDosesFlow: StateFlow<Int> = medications
        .map { meds -> meds.count { it.status == "Taken" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDosesFlow: StateFlow<Int> = medications
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markDose(medicationId: Int, time: String, isTaken: Boolean) {
        viewModelScope.launch {
            repository.setDoseStatus(
                DoseStatusEntity(
                    medicationId = medicationId,
                    time = time,
                    date = today,
                    isTaken = isTaken
                )
            )
        }
    }
}

class DashboardViewModelFactory(private val repository: MedicationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
