package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.Repository
import com.example.dosely.data.ExampleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: Repository) : ViewModel() {
    private val _history = MutableStateFlow<List<ExampleEntity>>(emptyList())
    val history: StateFlow<List<ExampleEntity>> = _history

    fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getLocalData()
        }
    }
} 