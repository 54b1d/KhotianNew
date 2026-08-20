package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.domain.model.UserPermissions
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.usecase.GetAccountLedgerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

@HiltViewModel(assistedFactory = FinancialAccountLedgerViewModel.Factory::class)
class FinancialAccountLedgerViewModel @AssistedInject constructor(
    @Assisted private val accountId: Long,
    private val getAccountLedgerUseCase: GetAccountLedgerUseCase,
    private val accountRepository: FinancialAccountRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = getAccountLedgerUseCase(accountId)
        .map { (openingBalance, list) ->
            var currentBalance = openingBalance
            list.sortedBy { it.timestamp }.map { transaction ->
                val change = when (transaction.businessType) {
                    BusinessTransactionType.PAYMENT_RECEIVED -> transaction.amount
                    BusinessTransactionType.PAYMENT_MADE -> transaction.amount.negate()
                    BusinessTransactionType.TRANSFER -> {
                        if (transaction.toFinancialAccountId == accountId) transaction.amount
                        else transaction.amount.negate()
                    }
                    else -> BigDecimal.ZERO
                }
                currentBalance = currentBalance.add(change)
                TransactionItem(transaction, currentBalance)
            }.reversed()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val account: StateFlow<FinancialAccount?> = accountRepository.getAccountById(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    @AssistedFactory
    interface Factory {
        fun create(accountId: Long): FinancialAccountLedgerViewModel
    }
}
