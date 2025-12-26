package com.abdur.rahman.attendanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.model.AttendanceRecord
import com.abdur.rahman.attendanceapp.data.model.AttendanceStatus
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryDetailUiState(
    val date: String = "",
    val records: List<AttendanceRecord> = emptyList(),
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val holidayCount: Int = 0,
    val notMarkedCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HistoryDetailViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()
    
    fun loadDateDetails(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                date = date,
                isLoading = true
            )
            
            repository.getAttendanceByDate(date).collect { records ->
                val sortedRecords = records.sortedBy { it.studentName }
                
                _uiState.value = _uiState.value.copy(
                    records = sortedRecords,
                    presentCount = records.count { it.status == AttendanceStatus.PRESENT },
                    absentCount = records.count { it.status == AttendanceStatus.ABSENT },
                    holidayCount = records.count { it.status == AttendanceStatus.HOLIDAY },
                    notMarkedCount = records.count { it.status == AttendanceStatus.NOT_MARKED },
                    isLoading = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
