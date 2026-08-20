package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.domain.model.FinancialAccountType
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class FinancialAccountEntryViewModel @Inject constructor(
    private val accountRepository: FinancialAccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FinancialAccountUiState>(FinancialAccountUiState.Idle)
    val uiState: StateFlow<FinancialAccountUiState> = _uiState.asStateFlow()

    fun addAccount(name: String, type: FinancialAccountType, openingBalance: BigDecimal) {
        viewModelScope.launch {
            _uiState.value = FinancialAccountUiState.Loading
            try {
                val account = FinancialAccount(
                    name = name,
                    type = type,
                    openingBalance = openingBalance,
                    currentBalance = openingBalance
                )
                accountRepository.addAccount(account)
                _uiState.value = FinancialAccountUiState.Success
            } catch (e: Exception) {
                _uiState.value = FinancialAccountUiState.Error(e.message ?: "Failed to add account")
            }
        }
    }

    fun resetState() {
        _uiState.value = FinancialAccountUiState.Idle
    }

    val allAccounts = accountRepository.getAllAccounts()
}

sealed class FinancialAccountUiState {
    object Idle : FinancialAccountUiState()
    object Loading : FinancialAccountUiState()
    object Success : FinancialAccountUiState()
    data class Error(val message: String) : FinancialAccountUiState()
}
