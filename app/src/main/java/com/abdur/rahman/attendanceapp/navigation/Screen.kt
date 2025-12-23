package com.abdur.rahman.attendanceapp.navigation

sealed class Screen(val route: String) {
    object Attendance : Screen("attendance")
    object AddStudent : Screen("add_student")
    object History : Screen("history")
    object ManageStudents : Screen("manage_students")
}
