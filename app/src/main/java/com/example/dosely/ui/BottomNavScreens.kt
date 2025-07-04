package com.example.dosely.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {
    CenteredScreenContent(title = "Dashboard")
}

@Composable
fun MedicationsScreen() {
    CenteredScreenContent(title = "Medications")
}

@Composable
fun RemindersScreen() {
    CenteredScreenContent(title = "Reminders")
}

@Composable
fun HistoryScreen() {
    CenteredScreenContent(title = "History")
}

@Composable
fun SettingsScreen() {
    CenteredScreenContent(title = "Settings")
}

@Composable
private fun CenteredScreenContent(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to $title",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
    }
} 