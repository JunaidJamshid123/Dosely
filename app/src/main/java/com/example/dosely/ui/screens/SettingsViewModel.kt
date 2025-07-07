package com.example.dosely.ui.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow("Settings Placeholder")
    val settings: StateFlow<String> = _settings
} 