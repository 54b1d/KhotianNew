package com.sabid.khotianv2.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.UserPermissions
import com.sabid.khotianv2.domain.repository.BusinessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyEntryViewModel @Inject constructor(
    private val repository: BusinessRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    var name by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var address by mutableStateOf("")
    var type by mutableStateOf("CUSTOMER")
    
    var isSubmitting by mutableStateOf(false)
        private set

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    fun saveParty(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            isSubmitting = true
            val party = Party(
                name = name,
                phoneNumber = phoneNumber.ifBlank { null },
                address = address.ifBlank { null },
                type = type
            )
            repository.addParty(party)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Unknown error") }
            isSubmitting = false
        }
    }
}
