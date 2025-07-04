package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dosely.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: Repository) : ViewModel() {
    private val _data = MutableStateFlow<List<String>>(emptyList())
    val data: StateFlow<List<String>> = _data

    fun loadRemoteData() {
        viewModelScope.launch {
            _data.value = repository.fetchRemoteData()
        }
    }
} 