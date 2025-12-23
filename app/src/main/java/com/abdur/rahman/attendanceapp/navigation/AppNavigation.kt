package com.abdur.rahman.attendanceapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abdur.rahman.attendanceapp.ui.screens.AddStudentScreen
import com.abdur.rahman.attendanceapp.ui.screens.AttendanceScreen
import com.abdur.rahman.attendanceapp.ui.screens.HistoryScreen
import com.abdur.rahman.attendanceapp.ui.screens.ManageStudentsScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Attendance.route
    ) {
        composable(Screen.Attendance.route) {
            AttendanceScreen(
                onNavigateToAddStudent = {
                    navController.navigate(Screen.AddStudent.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToManageStudents = {
                    navController.navigate(Screen.ManageStudents.route)
                }
            )
        }
        
        composable(Screen.AddStudent.route) {
            AddStudentScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ManageStudents.route) {
            ManageStudentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddStudent = {
                    navController.navigate(Screen.AddStudent.route)
                }
            )
        }
    }
}
