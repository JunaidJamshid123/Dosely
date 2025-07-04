package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.Repository
import com.example.dosely.data.ExampleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MedicationsViewModel(private val repository: Repository) : ViewModel() {
    private val _localData = MutableStateFlow<List<ExampleEntity>>(emptyList())
    val localData: StateFlow<List<ExampleEntity>> = _localData

    fun loadLocalData() {
        viewModelScope.launch {
            _localData.value = repository.getLocalData()
        }
    }
} 