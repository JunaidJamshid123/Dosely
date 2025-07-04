package com.example.dosely.ui

import androidx.annotation.DrawableRes

// Use @DrawableRes for resource icons

data class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int
) 