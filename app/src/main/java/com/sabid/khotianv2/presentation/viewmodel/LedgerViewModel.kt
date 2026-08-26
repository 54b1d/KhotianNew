package com.sabid.khotianv2.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.BusinessRepository
import com.sabid.khotianv2.domain.repository.UnitRepository
import com.sabid.khotianv2.domain.usecase.GetUnifiedLedgerUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class TransactionItem(
    val transaction: Transaction,
    val runningBalance: BigDecimal,
    val unitSymbol: String? = null,
    val otherPartyName: String? = null,
    val productName: String? = null,
    val isCredit: Boolean = false
)

@HiltViewModel(assistedFactory = LedgerViewModel.Factory::class)
class LedgerViewModel @AssistedInject constructor(
    @Assisted private val partyId: Long,
    private val getUnifiedLedgerUseCase: GetUnifiedLedgerUseCase,
    private val businessRepository: BusinessRepository,
    private val unitRepository: UnitRepository,
    private val productRepository: com.sabid.khotianv2.domain.repository.ProductRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val party: StateFlow<Party?> = businessRepository.getParty(partyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionItem>> = combine(
        getUnifiedLedgerUseCase(partyId),
        unitRepository.getAllUnits(),
        businessRepository.getParties(),
        productRepository.getAllProducts(),
        _selectedMonth
    ) { (openingBalance, list), units, parties, products, targetMonth ->
        var currentBalance = openingBalance
        val allProcessed = list.sortedBy { it.timestamp }.map { transaction ->
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
                TransactionType.EQUITY -> {
                    when (transaction.businessType) {
                        BusinessTransactionType.EQUITY_WITHDRAWAL -> transaction.netCost
                        BusinessTransactionType.EQUITY_CONTRIBUTION, BusinessTransactionType.PROFIT_DISTRIBUTION -> transaction.netCost.negate()
                        else -> BigDecimal.ZERO
                    }
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
                TransactionType.EQUITY -> transaction.businessType == BusinessTransactionType.EQUITY_CONTRIBUTION || 
                                          transaction.businessType == BusinessTransactionType.PROFIT_DISTRIBUTION
                else -> false
            }
            
            val productName = products.find { it.id == transaction.productId }?.name
            
            TransactionItem(transaction, currentBalance, unit?.symbol, otherPartyName, productName, isCredit)
        }

        allProcessed.filter { 
            val date = Instant.ofEpochMilli(it.transaction.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            YearMonth.from(date) == targetMonth
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balance: StateFlow<BigDecimal> = businessRepository.getPartyBalance(partyId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    @AssistedFactory
    interface Factory {
        fun create(partyId: Long): LedgerViewModel
    }
}
