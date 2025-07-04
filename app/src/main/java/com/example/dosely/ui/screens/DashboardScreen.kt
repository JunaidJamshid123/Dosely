package com.example.dosely.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = DashboardViewModel()) {
    val medications by viewModel.medications.collectAsState()
    val taken = viewModel.takenDoses
    val total = viewModel.totalDoses
    val progress = if (total > 0) taken / total.toFloat() else 0f
    val userName = "Alex"
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    // Using the same colors as splash screen
    val lightBlue = Color(0xFFDAF4FF)
    val mediumBlue = Color(0xFFB0D3E2)
    val darkBlue = Color(0xFF2E7BB8)
    val accentBlue = Color(0xFF4A90E2)

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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
        }

        // Progress Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        Text(
                            text = "$taken/$total",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentBlue
                        )
                    }

                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = accentBlue,
                        trackColor = mediumBlue.copy(alpha = 0.3f)
                    )

                    val remaining = total - taken
                    Text(
                        text = if (remaining > 0) "$remaining doses remaining" else "All doses completed! 🎉",
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
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fire),
                        contentDescription = "Streak",
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "3 Day Streak!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue
                        )
                        Text(
                            text = "Keep up the great work",
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue
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
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Medications List
        items(medications) { med ->
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
                    // Pill Icon (always shown)
                    Image(
                        painter = painterResource(id = R.drawable.pill),
                        contentDescription = "Pill",
                        modifier = Modifier.size(24.dp)
                    )

                    // Medication Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = darkBlue,
                            maxLines = 1
                        )
                        Text(
                            text = med.time,
                            style = MaterialTheme.typography.bodyMedium,
                            color = mediumBlue,
                            fontSize = 13.sp
                        )
                    }

                    // Status Badge
                    Surface(
                        color = if (med.status == "Taken")
                            Color(0xFFE8F5E8)
                        else
                            Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = med.status,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (med.status == "Taken")
                                Color(0xFF2E7D32)
                            else
                                Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "$total",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = darkBlue
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
                                fontSize = 12.sp
                            )
                        }

                        // Taken
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "$taken",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = accentBlue
                            )
                            Text(
                                text = "Taken",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
                                fontSize = 12.sp
                            )
                        }

                        // Remaining
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${total - taken}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = mediumBlue,
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
}