package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.model.Role
import com.sabid.khotianv2.domain.model.User
import com.sabid.khotianv2.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserManagementUiState>(UserManagementUiState.Idle)
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    val hasAccess: StateFlow<Boolean> = permissionManager.userPermissions
        .map { it.hasPermission(PermissionType.CAN_MANAGE_USERS) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val roles: StateFlow<List<Role>> = userRepository.getAllRoles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createUser(username: String, pin: String, roleId: Long?) {
        viewModelScope.launch {
            _uiState.value = UserManagementUiState.Loading
            val user = User(id = "0", username = username, roleId = roleId, roleName = null, isActive = true)
            val result = userRepository.createUser(user, pin)
            if (result.isSuccess) {
                _uiState.value = UserManagementUiState.Success("User created successfully")
            } else {
                _uiState.value = UserManagementUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create user")
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
        }
    }

    fun upsertRole(id: Long = 0, name: String, permissions: List<PermissionType>) {
        viewModelScope.launch {
            _uiState.value = UserManagementUiState.Loading
            val role = Role(id = id, name = name, permissions = permissions)
            val result = userRepository.upsertRole(role)
            if (result.isSuccess) {
                _uiState.value = UserManagementUiState.Success("Role saved successfully")
            } else {
                _uiState.value = UserManagementUiState.Error(result.exceptionOrNull()?.message ?: "Failed to save role")
            }
        }
    }

    fun deleteRole(roleId: Long) {
        viewModelScope.launch {
            userRepository.deleteRole(roleId)
        }
    }

    fun clearState() {
        _uiState.value = UserManagementUiState.Idle
    }
}

sealed class UserManagementUiState {
    object Idle : UserManagementUiState()
    object Loading : UserManagementUiState()
    data class Success(val message: String) : UserManagementUiState()
    data class Error(val message: String) : UserManagementUiState()
}
