package com.abdur.rahman.attendanceapp.data.model

/**
 * Represents the attendance data for a single day
 */
data class DailyAttendance(
    val date: String = "", // Format: yyyy-MM-dd
    val records: List<AttendanceRecord> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
