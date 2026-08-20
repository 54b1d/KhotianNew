package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.AppUnit
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.model.TransactionType
import com.sabid.khotianv2.domain.model.UserPermissions
import com.sabid.khotianv2.domain.repository.BusinessRepository
import com.sabid.khotianv2.domain.repository.UnitRepository
import com.sabid.khotianv2.domain.usecase.GetUnifiedLedgerUseCase
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

data class TransactionItem(
    val transaction: Transaction,
    val runningBalance: BigDecimal,
    val unitSymbol: String? = null
)

@HiltViewModel(assistedFactory = LedgerViewModel.Factory::class)
class LedgerViewModel @AssistedInject constructor(
    @Assisted private val partyId: Long,
    private val getUnifiedLedgerUseCase: GetUnifiedLedgerUseCase,
    private val businessRepository: BusinessRepository,
    private val unitRepository: UnitRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val transactions: StateFlow<List<TransactionItem>> = combine(
        getUnifiedLedgerUseCase(partyId),
        unitRepository.getAllUnits()
    ) { (openingBalance, list), units ->
        var currentBalance = openingBalance
        list.sortedBy { it.timestamp }.map { transaction ->
            val change = if (transaction.type == TransactionType.DEBIT) transaction.netCost else transaction.netCost.negate()
            currentBalance = currentBalance.add(change)
            val unit = units.find { it.id == transaction.unitId }
            TransactionItem(transaction, currentBalance, unit?.symbol)
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<BigDecimal> = businessRepository.getPartyBalance(partyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    @AssistedFactory
    interface Factory {
        fun create(partyId: Long): LedgerViewModel
    }
}
