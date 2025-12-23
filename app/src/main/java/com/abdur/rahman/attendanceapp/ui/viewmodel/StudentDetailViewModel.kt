package com.abdur.rahman.attendanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.model.AttendanceRecord
import com.abdur.rahman.attendanceapp.data.model.AttendanceStatus
import com.abdur.rahman.attendanceapp.data.model.Student
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentDetailUiState(
    val student: Student? = null,
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    
    // Summary stats
    val totalDays: Int = 0,
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val holidayDays: Int = 0,
    val notMarkedDays: Int = 0,
    val attendancePercentage: Float = 0f
)

class StudentDetailViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(StudentDetailUiState())
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()
    
    fun loadStudentDetails(studentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load student info
                val student = repository.getStudentById(studentId)
                
                // Load all attendance records for this student
                repository.getStudentAttendanceHistory(studentId).collect { records ->
                    val presentDays = records.count { it.status == AttendanceStatus.PRESENT }
                    val absentDays = records.count { it.status == AttendanceStatus.ABSENT }
                    val holidayDays = records.count { it.status == AttendanceStatus.HOLIDAY }
                    val notMarkedDays = records.count { it.status == AttendanceStatus.NOT_MARKED }
                    val totalDays = records.size
                    
                    // Calculate attendance percentage (excluding holidays and not marked)
                    val attendableDays = presentDays + absentDays
                    val attendancePercentage = if (attendableDays > 0) {
                        (presentDays.toFloat() / attendableDays.toFloat()) * 100
                    } else {
                        0f
                    }
                    
                    _uiState.value = StudentDetailUiState(
                        student = student,
                        attendanceRecords = records.sortedByDescending { it.date },
                        isLoading = false,
                        totalDays = totalDays,
                        presentDays = presentDays,
                        absentDays = absentDays,
                        holidayDays = holidayDays,
                        notMarkedDays = notMarkedDays,
                        attendancePercentage = attendancePercentage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load student details"
                )
            }
        }
    }
}
