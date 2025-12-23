package com.abdur.rahman.attendanceapp.data.model

enum class AttendanceStatus {
    NOT_MARKED,  // Black - Default state
    PRESENT,     // Green - Confirmed attendance
    ABSENT,      // Red - Triggers SMS
    HOLIDAY      // Blue - Locks attendance
}
