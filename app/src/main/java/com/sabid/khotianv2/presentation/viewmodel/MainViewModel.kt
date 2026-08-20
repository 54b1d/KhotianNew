package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.data.local.SyncManager
import com.sabid.khotianv2.data.local.UpdateManager
import com.sabid.khotianv2.data.remote.model.AppReleaseDto
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.repository.UserRepository
import com.sabid.khotianv2.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val syncManager: SyncManager,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    private val _startRoute = MutableStateFlow<NavRoutes?>(null)
    val startRoute: StateFlow<NavRoutes?> = _startRoute.asStateFlow()

    init {
        syncManager.schedulePeriodicSync()
        determineStartRoute()
    }

    private fun determineStartRoute() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.value
            if (userId != null) {
                _startRoute.value = NavRoutes.Home
            } else {
                if (userRepository.hasUsers()) {
                    _startRoute.value = NavRoutes.Login
                } else {
                    _startRoute.value = NavRoutes.Setup
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val release = updateManager.checkForUpdates()
            if (release != null) {
                _updateState.value = UpdateUiState.UpdateAvailable(release)
            }
        }
    }

    fun downloadAndInstallUpdate(release: AppReleaseDto) {
        viewModelScope.launch {
            _updateState.value = UpdateUiState.Downloading
            val result = updateManager.downloadAndInstallApk(release)
            if (result.isFailure) {
                _updateState.value = UpdateUiState.Error(result.exceptionOrNull()?.message ?: "Download failed")
            }
        }
    }

    fun dismissUpdate() {
        _updateState.value = UpdateUiState.Idle
    }
}

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    data class UpdateAvailable(val release: AppReleaseDto) : UpdateUiState()
    object Downloading : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}
