package com.abdur.rahman.attendanceapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.model.AttendanceRecord
import com.abdur.rahman.attendanceapp.data.model.AttendanceStatus
import com.abdur.rahman.attendanceapp.data.model.Student
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import com.abdur.rahman.attendanceapp.util.SmsHelper
import com.abdur.rahman.attendanceapp.util.SmsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class StudentAttendanceItem(
    val student: Student,
    val attendanceRecord: AttendanceRecord?,
    val status: AttendanceStatus = attendanceRecord?.status ?: AttendanceStatus.NOT_MARKED,
    val isSelected: Boolean = false, // For two-tap confirmation
    val pendingStatus: AttendanceStatus? = null // Status waiting for confirmation
)

// Snackbar message types
sealed class SnackbarMessage {
    data class Success(val message: String) : SnackbarMessage()
    data class Error(val message: String) : SnackbarMessage()
}

data class AttendanceUiState(
    val students: List<StudentAttendanceItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val todayDate: String = "",
    val smsError: SmsErrorInfo? = null, // For SMS failure dialog
    val snackbarMessage: SnackbarMessage? = null // For snackbar notifications
)

// SMS Error information for dialog
data class SmsErrorInfo(
    val studentName: String,
    val parentPhone: String,
    val errorMessage: String
)

class AttendanceViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()
    
    private val _selectedStudentId = MutableStateFlow<String?>(null)
    private val _pendingStatus = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    
    private var hasInitializedToday = false
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                todayDate = repository.getTodayDate()
            )
            
            // Combine students and attendance data
            combine(
                repository.getActiveStudents().distinctUntilChanged(),
                repository.getTodayAttendance().distinctUntilChanged(),
                _selectedStudentId,
                _pendingStatus
            ) { students, attendance, selectedId, pending ->
                
                // Initialize daily attendance only once per session
                if (students.isNotEmpty() && !hasInitializedToday) {
                    hasInitializedToday = true
                    initializeDailyAttendance(students)
                }
                
                val attendanceMap = attendance.associateBy { it.studentId }
                
                // Use distinctBy to ensure no duplicate students
                students.distinctBy { it.id }.map { student ->
                    val record = attendanceMap[student.id]
                    StudentAttendanceItem(
                        student = student,
                        attendanceRecord = record,
                        status = record?.status ?: AttendanceStatus.NOT_MARKED,
                        isSelected = selectedId == student.id,
                        pendingStatus = pending[student.id]
                    )
                }
            }.collect { items ->
                _uiState.value = _uiState.value.copy(
                    students = items,
                    isLoading = false
                )
            }
        }
    }
    
    private suspend fun initializeDailyAttendance(students: List<Student>) {
        if (!repository.isTodayInitialized()) {
            repository.initializeDailyAttendance(students)
        }
    }
    
    /**
     * Handle attendance button tap (Present/Absent)
     * First tap: Select and show pending
     * Second tap: Confirm and save
     */
    fun onAttendanceTap(
        context: Context,
        student: Student,
        status: AttendanceStatus
    ) {
        viewModelScope.launch {
            val selectedId = _selectedStudentId.value
            val pendingMap = _pendingStatus.value
            val currentPending = pendingMap[student.id]
            
            if (selectedId == student.id && currentPending == status) {
                // Second tap - confirm the attendance
                confirmAttendance(context, student, status)
            } else {
                // First tap - select and set pending status
                _selectedStudentId.value = student.id
                _pendingStatus.value = pendingMap + (student.id to status)
            }
        }
    }
    
    private suspend fun confirmAttendance(
        context: Context,
        student: Student,
        status: AttendanceStatus
    ) {
        // For ABSENT status, send SMS first before updating database
        if (status == AttendanceStatus.ABSENT) {
            // Check if SMS should be sent (not already sent today)
            val shouldSendSms = repository.shouldSendSms(student.id)
            
            if (shouldSendSms) {
                val smsResult = SmsHelper.sendAbsenceSms(
                    context = context,
                    phoneNumber = student.parentPhone,
                    studentName = student.name
                )
                
                when (smsResult) {
                    is SmsResult.Success -> {
                        // Show success snackbar
                        _uiState.value = _uiState.value.copy(
                            snackbarMessage = SnackbarMessage.Success(
                                "SMS sent to parent of ${smsResult.studentName}"
                            )
                        )
                    }
                    is SmsResult.Error -> {
                        // SMS failed - show error dialog and don't update database
                        _uiState.value = _uiState.value.copy(
                            smsError = SmsErrorInfo(
                                studentName = student.name,
                                parentPhone = student.parentPhone,
                                errorMessage = "Failed to send absence notification SMS to ${student.parentPhone}. Attendance not marked."
                            )
                        )
                        // Clear selection but don't update attendance
                        _selectedStudentId.value = null
                        _pendingStatus.value = _pendingStatus.value - student.id
                        return
                    }
                    is SmsResult.PermissionDenied -> {
                        // Permission denied - show error snackbar
                        _uiState.value = _uiState.value.copy(
                            snackbarMessage = SnackbarMessage.Error("SMS permission not granted")
                        )
                        _selectedStudentId.value = null
                        _pendingStatus.value = _pendingStatus.value - student.id
                        return
                    }
                }
            }
        }
        
        // SMS sent successfully (or not needed) - now update database
        val result = repository.updateAttendance(
            studentId = student.id,
            studentName = student.name,
            status = status
        )
        
        result.onSuccess { shouldSendSms ->
            // Clear selection
            _selectedStudentId.value = null
            _pendingStatus.value = _pendingStatus.value - student.id
            
            // Mark SMS as sent if it was an absent marking
            if (status == AttendanceStatus.ABSENT && shouldSendSms) {
                repository.markSmsSent(student.id)
            }
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                error = error.message ?: "Failed to update attendance"
            )
        }
    }
    
    /**
     * Clear SMS error dialog
     */
    fun clearSmsError() {
        _uiState.value = _uiState.value.copy(smsError = null)
    }
    
    /**
     * Clear snackbar message
     */
    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }
    
    /**
     * Clear selection when user taps elsewhere
     */
    fun clearSelection() {
        _selectedStudentId.value = null
        _pendingStatus.value = emptyMap()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
