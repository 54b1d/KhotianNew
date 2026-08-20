package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.usecase.ExportDataUseCase
import com.sabid.khotianv2.domain.usecase.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun exportData(onDataReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Exporting data...")
            try {
                val jsonData = exportDataUseCase()
                onDataReady(jsonData)
                _uiState.value = BackupUiState.Success("Data exported successfully")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun importData(jsonData: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading("Importing data...")
            try {
                importDataUseCase(jsonData)
                _uiState.value = BackupUiState.Success("Data imported successfully")
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error("Import failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Loading(val message: String) : BackupUiState()
    data class Success(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
