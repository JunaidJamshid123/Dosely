package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.MedicationEntity
import com.example.dosely.data.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationsViewModel(
    private val repository: MedicationRepository
) : ViewModel() {
    val medications: StateFlow<List<MedicationEntity>> =
        repository.getAllMedications().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun addMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.insertMedication(medication)
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.updateMedication(medication)
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }
} 