package com.abdur.rahman.attendanceapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Dynamic colors for bottom navigation bar
object BottomNavColors {
    // Light theme colors
    val LightBackground = Color(0xFF1565C0) // Rich blue
    val LightSelectedIcon = Color.White
    val LightSelectedText = Color.White
    val LightUnselectedIcon = Color.White.copy(alpha = 0.7f)
    val LightUnselectedText = Color.White.copy(alpha = 0.7f)
    val LightIndicator = Color.White.copy(alpha = 0.2f)
    
    // Dark theme colors - lighter combination
    val DarkBackground = Color(0xFF2D3250) // Lighter dark blue-gray
    val DarkSelectedIcon = Color(0xFFBBDEFB) // Brighter light blue
    val DarkSelectedText = Color(0xFFBBDEFB)
    val DarkUnselectedIcon = Color(0xFFB0B0B0) // Lighter gray
    val DarkUnselectedText = Color(0xFFB0B0B0)
    val DarkIndicator = Color(0xFFBBDEFB).copy(alpha = 0.2f)
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Attendance : BottomNavItem(
        route = "attendance",
        title = "Attendance",
        selectedIcon = Icons.Filled.CheckCircle,
        unselectedIcon = Icons.Outlined.CheckCircle
    )
    
    object Students : BottomNavItem(
        route = "students",
        title = "Students",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    )
    
    object History : BottomNavItem(
        route = "history",
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
}

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    
    val bottomNavItems = listOf(
        BottomNavItem.Attendance,
        BottomNavItem.Students,
        BottomNavItem.History
    )
    
    // Dynamic colors based on theme
    val navBackground = if (isDarkTheme) BottomNavColors.DarkBackground else BottomNavColors.LightBackground
    val selectedIconColor = if (isDarkTheme) BottomNavColors.DarkSelectedIcon else BottomNavColors.LightSelectedIcon
    val selectedTextColor = if (isDarkTheme) BottomNavColors.DarkSelectedText else BottomNavColors.LightSelectedText
    val unselectedIconColor = if (isDarkTheme) BottomNavColors.DarkUnselectedIcon else BottomNavColors.LightUnselectedIcon
    val unselectedTextColor = if (isDarkTheme) BottomNavColors.DarkUnselectedText else BottomNavColors.LightUnselectedText
    val indicatorColor = if (isDarkTheme) BottomNavColors.DarkIndicator else BottomNavColors.LightIndicator
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = navBackground,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedIconColor,
                            selectedTextColor = selectedTextColor,
                            unselectedIconColor = unselectedIconColor,
                            unselectedTextColor = unselectedTextColor,
                            indicatorColor = indicatorColor
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Attendance.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Attendance.route) {
                AttendanceScreenContent(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
            
            composable(BottomNavItem.Students.route) {
                StudentsScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onStudentClick = { studentId, studentName ->
                        navController.navigate("student_detail/$studentId/$studentName")
                    }
                )
            }
            
            composable(BottomNavItem.History.route) {
                HistoryScreenContent(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
            
            // Student Detail Screen
            composable("student_detail/{studentId}/{studentName}") { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
                val studentName = backStackEntry.arguments?.getString("studentName") ?: ""
                StudentDetailScreen(
                    studentId = studentId,
                    studentName = studentName,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
