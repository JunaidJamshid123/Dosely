package com.example.dosely.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.painterResource
import com.example.dosely.R
import android.content.Context
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.dosely.data.AppDatabase
import com.example.dosely.data.MedicationRepositoryImpl
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.clickable
import com.example.dosely.data.MedicationEntity
import com.example.dosely.ui.screens.MedicationEntryDetailsDialog
import com.example.dosely.ui.screens.MedicationEntry

@Composable
fun DashboardScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val owner = LocalViewModelStoreOwner.current

    // Color scheme matching dashboard
    val lightBlue = Color(0xFFDAF4FF)
    val mediumBlue = Color(0xFFB0D3E2)
    val darkBlue = Color(0xFF2E7BB8)
    val accentBlue = Color(0xFF4A90E2)

    val selectedMedication = remember { mutableStateOf<MedicationEntry?>(null) }
    val showDetailsDialog = remember { mutableStateOf(false) }

    if (owner == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(lightBlue, lightBlue.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accentBlue)
        }
        return
    }

    // Build Room database and repository
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "dosely-db"
        ).build()
    }
    val repository = remember { MedicationRepositoryImpl(db.medicationDao(), db.doseStatusDao()) }
    val factory = remember { DashboardViewModelFactory(repository) }
    val viewModel: DashboardViewModel = viewModel(
        viewModelStoreOwner = owner,
        factory = factory
    )

    val medications by viewModel.medications.collectAsState()
    val taken by viewModel.takenDosesFlow.collectAsState()
    val total by viewModel.totalDosesFlow.collectAsState()
    val progress = if (total > 0) taken / total.toFloat() else 0f
    val userName = "Alex"
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lightBlue,
                        lightBlue.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section with Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Hello, $userName! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = darkBlue
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = mediumBlue,
                        fontSize = 16.sp
                    )
                }

                // Profile Icon
                Card(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, accentBlue.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = accentBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Progress Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue
                        )
                        Surface(
                            color = accentBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$taken/$total",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = accentBlue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentBlue,
                        trackColor = mediumBlue.copy(alpha = 0.2f)
                    )

                    val remaining = total - taken
                    val progressText = if (remaining > 0) {
                        "$remaining doses remaining"
                    } else {
                        "All doses completed! 🎉"
                    }

                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = mediumBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Streak Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.fire),
                                contentDescription = "Streak",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "3 Day Streak!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue
                        )
                        Text(
                            text = "Keep up the great work",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Medications Section Header
        item {
            Text(
                text = "Today's Medications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = darkBlue,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Medications List
        items(medications) { med ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedMedication.value = med
                        showDetailsDialog.value = true
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pill Icon with background
                    Surface(
                        color = lightBlue.copy(alpha = 0.5f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pill),
                                contentDescription = "Pill",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Medication Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = med.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (med.status == "Taken") Color(0xFFE8F5E8) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = med.status,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (med.status == "Taken") Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {
                                viewModel.markDose(
                                    med.medicationId,
                                    med.timeRaw,
                                    isTaken = med.status != "Taken"
                                )
                            },
                            label = { Text(if (med.status == "Taken") "Undo" else "Done", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    if (med.status == "Taken") Icons.Filled.Undo else Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (med.status == "Taken") Color(0xFFE57373) else Color(0xFF4A90E2),
                                labelColor = Color.White,
                                leadingIconContentColor = Color.White
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }
        }

        // If the medications list is empty, show a friendly empty state
        if (medications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No medications for today!",
                        color = mediumBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Statistics Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Today's Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = darkBlue
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Total
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = darkBlue.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$total",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = darkBlue
                                    )
                                }
                            }
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        // Taken
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = accentBlue.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$taken",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = accentBlue
                                    )
                                }
                            }
                            Text(
                                text = "Taken",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        // Remaining
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = Color(0xFFE57373).copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${total - taken}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE57373)
                                    )
                                }
                            }
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDetailsDialog.value && selectedMedication.value != null) {
        MedicationEntryDetailsDialog(
            medication = selectedMedication.value!!,
            onDismiss = { showDetailsDialog.value = false }
        )
    }
}