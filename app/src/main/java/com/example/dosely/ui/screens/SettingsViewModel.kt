package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import com.example.dosely.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: Repository) : ViewModel() {
    private val _settings = MutableStateFlow("Settings Placeholder")
    val settings: StateFlow<String> = _settings
} 