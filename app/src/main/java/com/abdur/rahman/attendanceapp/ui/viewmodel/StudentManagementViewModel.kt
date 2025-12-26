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
    val editingStudent: Student? = null,
    val editName: String = "",
    val editPhone: String = "",
    val editNameError: String? = null,
    val editPhoneError: String? = null,
    val isEditLoading: Boolean = false,
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
    
    // ==================== EDIT STUDENT OPERATIONS ====================
    
    fun showEditDialog(student: Student) {
        _uiState.value = _uiState.value.copy(
            editingStudent = student,
            editName = student.name,
            editPhone = student.parentPhone,
            editNameError = null,
            editPhoneError = null
        )
    }
    
    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            editingStudent = null,
            editName = "",
            editPhone = "",
            editNameError = null,
            editPhoneError = null
        )
    }
    
    fun onEditNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            editName = name,
            editNameError = null
        )
    }
    
    fun onEditPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            editPhone = phone,
            editPhoneError = null
        )
    }
    
    fun updateStudent() {
        val currentState = _uiState.value
        val student = currentState.editingStudent ?: return
        val name = currentState.editName.trim()
        val phone = currentState.editPhone.trim()
        
        // Validation
        var hasError = false
        var nameError: String? = null
        var phoneError: String? = null
        
        if (name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        } else if (name.length < 2) {
            nameError = "Name must be at least 2 characters"
            hasError = true
        }
        
        if (phone.isBlank()) {
            phoneError = "Phone number is required"
            hasError = true
        } else if (!phone.matches(Regex("^[+]?[0-9]{10,15}$"))) {
            phoneError = "Invalid phone number format"
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = _uiState.value.copy(
                editNameError = nameError,
                editPhoneError = phoneError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isEditLoading = true)
            
            val result = repository.updateStudent(student.id, name, phone)
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isEditLoading = false,
                    editingStudent = null,
                    editName = "",
                    editPhone = ""
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isEditLoading = false,
                    error = error.message ?: "Failed to update student"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
