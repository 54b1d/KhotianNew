package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.data.local.SampleDataGenerator
import com.sabid.khotianv2.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sampleDataGenerator: SampleDataGenerator
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setup(adminUsername: String, adminPin: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            userRepository.initializeSystem(adminUsername, adminPin)
                .onSuccess { _uiState.value = AuthUiState.Success }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Setup failed") }
        }
    }

    fun bypassAndPreload() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                sampleDataGenerator.generateSampleData()
                _uiState.value = AuthUiState.Success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Preload failed")
            }
        }
    }
}
