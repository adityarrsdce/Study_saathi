package com.babu.appp.Navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar_V1(navController: NavController) {
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    val isDarkMode = isSystemInDarkTheme()
    val selectedColor = Color(0xFFE5D1B5)
    val unselectedColor = if (isDarkMode) Color(0xFFB0B0B0) else Color(0xFF000000)
    val containerColor = if (isDarkMode) Color(0xFF121212) else Color.White

    val items = listOf(
        Triple("home", "Study", Icons.Default.School),
        Triple("ranking", "Ranking", Icons.Default.BarChart),
        Triple("college", "Holidays", Icons.Default.HolidayVillage),
        Triple("events", "Calendar", Icons.Default.Event),
        Triple("about", "About", Icons.Default.Info)
    )

    NavigationBar(containerColor = containerColor) {
        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route

            val tint by animateColorAsState(
                if (isSelected) selectedColor else unselectedColor,
                label = ""
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route) {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = label, tint = tint)
                        Text(label, fontSize = 10.sp, color = tint)
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
