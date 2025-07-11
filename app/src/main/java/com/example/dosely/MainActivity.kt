package com.example.dosely

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dosely.ui.theme.DoselyTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dosely.ui.BottomNavItem
import com.example.dosely.ui.screens.DashboardScreen
import com.example.dosely.ui.screens.MedicationsScreen
import com.example.dosely.ui.screens.RemindersScreen
import com.example.dosely.ui.screens.HistoryScreen
import com.example.dosely.ui.screens.SettingsScreen
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
        enableEdgeToEdge()
        setContent {
            DoselyTheme {
                val navController = rememberNavController()
                val bottomNavItems = listOf(
                    BottomNavItem("dashboard", "Dashboard", R.drawable.homme),
                    BottomNavItem("medications", "Medications", R.drawable.pill),
                    BottomNavItem("reminders", "Reminders", R.drawable.clock),
                    BottomNavItem("history", "History", R.drawable.historyy),
                    BottomNavItem("settings", "Settings", R.drawable.settingsa)
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavigationBar(
                            items = bottomNavItems,
                            navController = navController,
                            onItemClick = { item ->
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("dashboard") { DashboardScreen() }
                        composable("medications") { MedicationsScreen() }
                        composable("reminders") { RemindersScreen() }
                        composable("history") { HistoryScreen() }
                        composable("settings") { SettingsScreen() }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DoselyTheme {
        Greeting("Android")
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController,
    onItemClick: (BottomNavItem) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val colorScheme = MaterialTheme.colorScheme
    val selectedColor = colorScheme.primary
    val unselectedColor = colorScheme.onSurface.copy(alpha = 0.6f)
    val backgroundColor = colorScheme.surface

    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp),
                        tint = if (selected) selectedColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        maxLines = 1,
                        color = if (selected) selectedColor else unselectedColor
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}