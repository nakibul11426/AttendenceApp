package com.abdur.rahman.attendanceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdur.rahman.attendanceapp.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddStudentUiState(
    val name: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val phoneError: String? = null
)

class AddStudentViewModel : ViewModel() {
    
    private val repository = AttendanceRepository()
    
    private val _uiState = MutableStateFlow(AddStudentUiState())
    val uiState: StateFlow<AddStudentUiState> = _uiState.asStateFlow()
    
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }
    
    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phone,
            phoneError = null
        )
    }
    
    fun addStudent() {
        val state = _uiState.value
        
        // Validate inputs
        var hasError = false
        
        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            hasError = true
        }
        
        if (state.phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(phoneError = "Phone number is required")
            hasError = true
        } else if (!isValidPhoneNumber(state.phoneNumber)) {
            _uiState.value = _uiState.value.copy(phoneError = "Invalid phone number")
            hasError = true
        }
        
        if (hasError) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val result = repository.addStudent(
                name = state.name.trim(),
                parentPhone = state.phoneNumber.trim()
            )
            
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to add student"
                )
            }
        }
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic validation - at least 10 digits
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length >= 10
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun resetState() {
        _uiState.value = AddStudentUiState()
    }
}
