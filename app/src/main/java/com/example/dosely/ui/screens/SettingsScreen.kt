package com.example.dosely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F8FF))
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color(0xFF2E3A59)
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Section
            item {
                SettingsSection(title = "Profile") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Personal Information",
                        subtitle = "Name, age, medical details",
                        onClick = { /* Navigate to profile */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Privacy & Security",
                        subtitle = "Password, biometric settings",
                        onClick = { /* Navigate to privacy */ }
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    var notificationsEnabled by remember { mutableStateOf(true) }
                    var soundEnabled by remember { mutableStateOf(true) }
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Medication Reminders",
                        subtitle = "Get notified when it's time to take medication",
                        isChecked = notificationsEnabled,
                        onToggle = { notificationsEnabled = it }
                    )
                    SettingsToggleItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Sound Alerts",
                        subtitle = "Play sound with notifications",
                        isChecked = soundEnabled,
                        onToggle = { soundEnabled = it }
                    )
                }
            }

            // Data & Storage Section
            item {
                SettingsSection(title = "Data & Storage") {
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Backup & Sync",
                        subtitle = "Cloud backup settings",
                        onClick = { /* Navigate to backup */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        subtitle = "Download your medication history",
                        onClick = { /* Export data */ }
                    )
                }
            }

            // Support Section
            item {
                SettingsSection(title = "Support") {
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help Center",
                        subtitle = "FAQs and guides",
                        onClick = { /* Navigate to help */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Email,
                        title = "Contact Support",
                        subtitle = "Get help from our team",
                        onClick = { /* Contact support */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate App",
                        subtitle = "Share your feedback",
                        onClick = { /* Rate app */ }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "v1.0.0",
                        onClick = { /* Show version info */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "Legal information",
                        onClick = { /* Show terms */ }
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = { /* Show privacy policy */ }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E3A59)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E3A59)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF8A8A8A)
                )
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF8A8A8A),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E3A59)
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF8A8A8A)
                )
            )
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4A90E2),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}