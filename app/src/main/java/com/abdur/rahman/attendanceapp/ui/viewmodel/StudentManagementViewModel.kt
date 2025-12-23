package com.abdur.rahman.attendanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.model.Student
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentManagementUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteConfirmation: Student? = null,
    val error: String? = null
)

class StudentManagementViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(StudentManagementUiState())
    val uiState: StateFlow<StudentManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadStudents()
    }
    
    private fun loadStudents() {
        viewModelScope.launch {
            repository.getActiveStudents().collect { students ->
                _uiState.value = _uiState.value.copy(
                    students = students,
                    isLoading = false
                )
            }
        }
    }
    
    fun showDeleteConfirmation(student: Student) {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = student)
    }
    
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = null)
    }
    
    fun removeStudent(student: Student) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showDeleteConfirmation = null)
            
            val result = repository.removeStudent(student.id)
            
            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Failed to remove student"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
