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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal

@HiltViewModel(assistedFactory = PartyEntryViewModel.Factory::class)
class PartyEntryViewModel @AssistedInject constructor(
    private val repository: BusinessRepository,
    private val permissionManager: PermissionManager,
    @Assisted private val partyId: Long?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(partyId: Long?): PartyEntryViewModel
    }

    val isEditMode = partyId != null

    var name by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var address by mutableStateOf("")
    var type by mutableStateOf("CUSTOMER")
    var openingBalance by mutableStateOf("")
    
    var isSubmitting by mutableStateOf(false)
        private set

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    init {
        if (partyId != null) {
            viewModelScope.launch {
                repository.getParty(partyId).firstOrNull()?.let { party ->
                    name = party.name
                    phoneNumber = party.phoneNumber ?: ""
                    address = party.address ?: ""
                    type = party.type
                    openingBalance = party.openingBalance.toPlainString()
                }
            }
        }
    }

    fun saveParty(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (name.isBlank()) {
            onError("Name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            isSubmitting = true
            val party = Party(
                id = partyId ?: 0L,
                name = name,
                phoneNumber = phoneNumber.ifBlank { null },
                address = address.ifBlank { null },
                type = type,
                openingBalance = openingBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO
            )
            repository.addParty(party)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Unknown error") }
            isSubmitting = false
        }
    }
}
