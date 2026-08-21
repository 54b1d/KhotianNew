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
    val unitSymbol: String? = null,
    val otherPartyName: String? = null,
    val isCredit: Boolean = false
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
        unitRepository.getAllUnits(),
        businessRepository.getParties()
    ) { (openingBalance, list), units, parties ->
        var currentBalance = openingBalance
        list.sortedBy { it.timestamp }.map { transaction ->
            val change = when (transaction.type) {
                TransactionType.DEBIT -> transaction.netCost
                TransactionType.CREDIT -> transaction.netCost.negate()
                TransactionType.PARTY_SETTLEMENT -> {
                    if (transaction.partyId == partyId) {
                        transaction.netCost.negate()
                    } else if (transaction.toPartyId == partyId) {
                        transaction.netCost
                    } else BigDecimal.ZERO
                }
                else -> BigDecimal.ZERO
            }
            currentBalance = currentBalance.add(change)
            val unit = units.find { it.id == transaction.unitId }
            val otherPartyId = if (transaction.partyId == partyId) transaction.toPartyId else transaction.partyId
            val otherPartyName = if (transaction.type == TransactionType.PARTY_SETTLEMENT) {
                parties.find { it.id == otherPartyId }?.name
            } else null
            
            val isCredit = when (transaction.type) {
                TransactionType.CREDIT -> true
                TransactionType.DEBIT -> false
                TransactionType.PARTY_SETTLEMENT -> transaction.partyId == partyId
                else -> false
            }
            
            TransactionItem(transaction, currentBalance, unit?.symbol, otherPartyName, isCredit)
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
