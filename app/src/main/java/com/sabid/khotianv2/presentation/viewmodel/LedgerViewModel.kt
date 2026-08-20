package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.model.TransactionType
import com.sabid.khotianv2.domain.model.UserPermissions
import com.sabid.khotianv2.domain.repository.BusinessRepository
import com.sabid.khotianv2.domain.usecase.GetUnifiedLedgerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal

data class TransactionItem(
    val transaction: Transaction,
    val runningBalance: BigDecimal
)

@HiltViewModel(assistedFactory = LedgerViewModel.Factory::class)
class LedgerViewModel @AssistedInject constructor(
    @Assisted private val partyId: Long,
    private val getUnifiedLedgerUseCase: GetUnifiedLedgerUseCase,
    private val businessRepository: BusinessRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val transactions: StateFlow<List<TransactionItem>> = getUnifiedLedgerUseCase(partyId)
        .map { list ->
            var currentBalance = BigDecimal.ZERO
            list.sortedBy { it.timestamp }.map { transaction ->
                val change = if (transaction.type == TransactionType.DEBIT) transaction.netCost else transaction.netCost.negate()
                currentBalance = currentBalance.add(change)
                TransactionItem(transaction, currentBalance)
            }.reversed()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<BigDecimal> = businessRepository.getPartyBalance(partyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    @AssistedFactory
    interface Factory {
        fun create(partyId: Long): LedgerViewModel
    }
}
