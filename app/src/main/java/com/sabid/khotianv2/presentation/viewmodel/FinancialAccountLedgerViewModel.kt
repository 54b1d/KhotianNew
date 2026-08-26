package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.domain.model.UserPermissions
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.usecase.GetAccountLedgerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

@HiltViewModel(assistedFactory = FinancialAccountLedgerViewModel.Factory::class)
class FinancialAccountLedgerViewModel @AssistedInject constructor(
    @Assisted private val accountId: Long,
    private val getAccountLedgerUseCase: GetAccountLedgerUseCase,
    private val accountRepository: FinancialAccountRepository,
    private val productRepository: ProductRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = combine(
        getAccountLedgerUseCase(accountId),
        productRepository.getAllProducts()
    ) { (openingBalance, list), products ->
        var currentBalance = openingBalance
        list.sortedBy { it.timestamp }.map { transaction ->
            val change = when (transaction.businessType) {
                BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> transaction.amount
                BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE, 
                BusinessTransactionType.EQUITY_WITHDRAWAL, BusinessTransactionType.PROFIT_DISTRIBUTION -> transaction.amount.negate()
                BusinessTransactionType.TRANSFER -> {
                    if (transaction.toFinancialAccountId == accountId) transaction.amount
                    else transaction.amount.negate()
                }
                else -> BigDecimal.ZERO
            }
            currentBalance = currentBalance.add(change)
            val productName = products.find { it.id == transaction.productId }?.name
            TransactionItem(transaction, currentBalance, productName = productName)
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
