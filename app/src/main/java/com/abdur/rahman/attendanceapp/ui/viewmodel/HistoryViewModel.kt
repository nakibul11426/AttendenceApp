package com.abdur.rahman.attendanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.model.AttendanceRecord
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val dates: List<String> = emptyList(),
    val selectedDate: String? = null,
    val attendanceRecords: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HistoryViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadDates()
    }
    
    private fun loadDates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.getAttendanceDates().collect { dates ->
                _uiState.value = _uiState.value.copy(
                    dates = dates,
                    isLoading = false
                )
            }
        }
    }
    
    fun selectDate(date: String) {
        if (_uiState.value.selectedDate == date) {
            // Deselect if already selected
            _uiState.value = _uiState.value.copy(
                selectedDate = null,
                attendanceRecords = emptyList()
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                isLoading = true
            )
            
            repository.getAttendanceByDate(date).collect { records ->
                _uiState.value = _uiState.value.copy(
                    attendanceRecords = records.sortedBy { it.studentName },
                    isLoading = false
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
