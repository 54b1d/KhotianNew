package com.sabid.khotianv2.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.BusinessRepository
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.repository.UnitRepository
import com.sabid.khotianv2.domain.repository.ExpenseCategoryRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import com.sabid.khotianv2.domain.usecase.AddTransactionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

sealed class UnifiedAccount {
    data class PartyAccount(val party: Party) : UnifiedAccount()
    data class Financial(val account: FinancialAccount) : UnifiedAccount()

    val id: Long
        get() = when (this) {
            is PartyAccount -> party.id
            is Financial -> account.id
        }

    val name: String
        get() = when (this) {
            is PartyAccount -> party.name
            is Financial -> account.name
        }
}

data class CostRow(
    val type: LinkedTransactionType = LinkedTransactionType.FREIGHT,
    val amount: String = "0",
    val sourceAccount: UnifiedAccount? = null,
    val destinationAccount: UnifiedAccount? = null,
    val note: String = ""
)

@HiltViewModel(assistedFactory = TransactionEntryViewModel.Factory::class)
class TransactionEntryViewModel @AssistedInject constructor(
    @Assisted("partyId") private val initialPartyId: Long?,
    @Assisted("transactionId") val transactionId: Long?,
    private val businessRepository: BusinessRepository,
    private val productRepository: ProductRepository,
    private val unitRepository: UnitRepository,
    private val financialAccountRepository: FinancialAccountRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val permissionManager: PermissionManager
) : ViewModel() {

    val parties: StateFlow<List<Party>> = businessRepository.getParties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val units: StateFlow<List<AppUnit>> = unitRepository.getAllUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val financialAccounts: StateFlow<List<FinancialAccount>> = financialAccountRepository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<ExpenseCategory>> = expenseCategoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unifiedAccounts: StateFlow<List<UnifiedAccount>> = combine(parties, financialAccounts) { pList, fList ->
        pList.map { UnifiedAccount.PartyAccount(it) } + fList.map { UnifiedAccount.Financial(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    var partyId by mutableStateOf<Long?>(initialPartyId)
    var toPartyId by mutableStateOf<Long?>(null)
    var productId by mutableStateOf<Long?>(null)
    var unitId by mutableStateOf<Long?>(null)
    var financialAccountId by mutableStateOf<Long?>(null)
    var toFinancialAccountId by mutableStateOf<Long?>(null)
    var expenseCategoryId by mutableStateOf<Long?>(null)
    var quantity by mutableStateOf("0")
    var rate by mutableStateOf("0")
    var amountText by mutableStateOf("0")
    var businessType by mutableStateOf(BusinessTransactionType.SALE)
    var note by mutableStateOf("")

    var showErrors by mutableStateOf(false)

    fun onQuantityChange(value: String) {
        quantity = value
        val q = try { BigDecimal(value) } catch (e: Exception) { BigDecimal.ZERO }
        val r = try { BigDecimal(rate) } catch (e: Exception) { BigDecimal.ZERO }
        val a = try { BigDecimal(amountText) } catch (e: Exception) { BigDecimal.ZERO }

        if (q.compareTo(BigDecimal.ZERO) != 0) {
            if (r.compareTo(BigDecimal.ZERO) != 0) {
                amountText = q.multiply(r).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            } else if (a.compareTo(BigDecimal.ZERO) != 0) {
                rate = a.divide(q, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }
        }
    }

    fun onRateChange(value: String) {
        rate = value
        val q = try { BigDecimal(quantity) } catch (e: Exception) { BigDecimal.ZERO }
        val r = try { BigDecimal(value) } catch (e: Exception) { BigDecimal.ZERO }
        val a = try { BigDecimal(amountText) } catch (e: Exception) { BigDecimal.ZERO }

        if (r.compareTo(BigDecimal.ZERO) != 0) {
            if (q.compareTo(BigDecimal.ZERO) != 0) {
                amountText = q.multiply(r).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            } else if (a.compareTo(BigDecimal.ZERO) != 0) {
                quantity = a.divide(r, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }
        }
    }

    fun onAmountInputChange(value: String) {
        amountText = value
        val q = try { BigDecimal(quantity) } catch (e: Exception) { BigDecimal.ZERO }
        val r = try { BigDecimal(rate) } catch (e: Exception) { BigDecimal.ZERO }
        val a = try { BigDecimal(value) } catch (e: Exception) { BigDecimal.ZERO }

        if (a.compareTo(BigDecimal.ZERO) != 0) {
            if (q.compareTo(BigDecimal.ZERO) != 0) {
                rate = a.divide(q, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            } else if (r.compareTo(BigDecimal.ZERO) != 0) {
                quantity = a.divide(r, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }
        }
    }

    var additionalCosts by mutableStateOf(listOf<CostRow>())

    fun addCostRow() {
        additionalCosts = additionalCosts + CostRow()
    }

    fun removeCostRow(index: Int) {
        additionalCosts = additionalCosts.toMutableList().apply { removeAt(index) }
    }

    fun updateCostRow(index: Int, newRow: CostRow) {
        additionalCosts = additionalCosts.toMutableList().apply { this[index] = newRow }
    }

    var sourceAccount by mutableStateOf<UnifiedAccount?>(null)
    var destinationAccount by mutableStateOf<UnifiedAccount?>(null)

    val isTransferMode: Boolean
        get() = businessType == BusinessTransactionType.TRANSFER ||
                businessType == BusinessTransactionType.PARTY_SETTLEMENT ||
                businessType == BusinessTransactionType.PAYMENT_MADE ||
                businessType == BusinessTransactionType.PAYMENT_RECEIVED ||
                businessType == BusinessTransactionType.EQUITY_CONTRIBUTION ||
                businessType == BusinessTransactionType.EQUITY_WITHDRAWAL

    fun onSourceAccountSelected(account: UnifiedAccount?) {
        sourceAccount = account
        updateInternalIdsFromUnified()
    }

    fun onDestinationAccountSelected(account: UnifiedAccount?) {
        destinationAccount = account
        updateInternalIdsFromUnified()
    }

    private fun updateInternalIdsFromUnified() {
        // Reset relevant IDs
        partyId = null
        toPartyId = null
        financialAccountId = null
        toFinancialAccountId = null

        val src = sourceAccount
        val dest = destinationAccount

        if (src == null || dest == null) return

        when (src) {
            is UnifiedAccount.PartyAccount -> partyId = src.id
            is UnifiedAccount.Financial -> financialAccountId = src.id
        }

        when (dest) {
            is UnifiedAccount.PartyAccount -> {
                if (src is UnifiedAccount.PartyAccount) {
                    toPartyId = dest.id
                    businessType = BusinessTransactionType.PARTY_SETTLEMENT
                } else {
                    partyId = dest.id
                    financialAccountId = (src as UnifiedAccount.Financial).id
                    val destParty = parties.value.find { it.id == dest.id }
                    businessType = if (destParty?.type == "PARTNER") BusinessTransactionType.EQUITY_WITHDRAWAL else BusinessTransactionType.PAYMENT_MADE
                }
            }
            is UnifiedAccount.Financial -> {
                if (src is UnifiedAccount.Financial) {
                    toFinancialAccountId = dest.id
                    businessType = BusinessTransactionType.TRANSFER
                } else {
                    partyId = (src as UnifiedAccount.PartyAccount).id
                    financialAccountId = dest.id
                    val srcParty = parties.value.find { it.id == src.id }
                    businessType = if (srcParty?.type == "PARTNER") BusinessTransactionType.EQUITY_CONTRIBUTION else BusinessTransactionType.PAYMENT_RECEIVED
                }
            }
        }
    }

    var isEditing by mutableStateOf(false)
        private set

    init {
        // Data loading is handled by TransactionEntryScreen using LaunchedEffect 
        // to ensure fresh data when the same ViewModel instance is reused 
        // (though we also use unique keys in HiltViewModel).
    }

    fun loadTransaction(id: Long?) {
        // Reset state to defaults or initial values
        partyId = initialPartyId
        toPartyId = null
        productId = null
        unitId = null
        financialAccountId = null
        toFinancialAccountId = null
        expenseCategoryId = null
        quantity = "0"
        rate = "0"
        amountText = "0"
        businessType = BusinessTransactionType.SALE
        note = ""
        sourceAccount = null
        destinationAccount = null
        additionalCosts = emptyList()
        
        if (id != null) {
            isEditing = true
            viewModelScope.launch {
                val tx = transactionRepository.getTransactionById(id)
                val children = transactionRepository.getChildTransactions(id)
                if (tx != null) {
                    partyId = tx.partyId
                    toPartyId = tx.toPartyId
                    productId = tx.productId
                    unitId = tx.unitId
                    financialAccountId = tx.financialAccountId
                    toFinancialAccountId = tx.toFinancialAccountId
                    expenseCategoryId = tx.expenseCategoryId
                    quantity = tx.quantity?.toPlainString() ?: "0"
                    rate = tx.rate?.toPlainString() ?: "0"
                    amountText = tx.amount.toPlainString()
                    businessType = tx.businessType
                    note = tx.note ?: ""

                    // Reconstruct additional costs
                    additionalCosts = children.map { child ->
                        var childSrc: UnifiedAccount? = null
                        var childDest: UnifiedAccount? = null

                        when (child.businessType) {
                            BusinessTransactionType.EXPENSE -> {
                                childSrc = financialAccounts.value.find { it.id == child.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                            }
                            BusinessTransactionType.PURCHASE -> {
                                childDest = parties.value.find { it.id == child.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                            }
                            BusinessTransactionType.PAYMENT_MADE -> {
                                childSrc = financialAccounts.value.find { it.id == child.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                                childDest = parties.value.find { it.id == child.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                            }
                            BusinessTransactionType.PAYMENT_RECEIVED -> {
                                childSrc = parties.value.find { it.id == child.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                                childDest = financialAccounts.value.find { it.id == child.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                            }
                            BusinessTransactionType.TRANSFER -> {
                                childSrc = financialAccounts.value.find { it.id == child.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                                childDest = financialAccounts.value.find { it.id == child.toFinancialAccountId }?.let { UnifiedAccount.Financial(it) }
                            }
                            BusinessTransactionType.PARTY_SETTLEMENT -> {
                                childSrc = parties.value.find { it.id == child.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                                childDest = parties.value.find { it.id == child.toPartyId }?.let { UnifiedAccount.PartyAccount(it) }
                            }
                            else -> {
                                // Fallback
                                childSrc = financialAccounts.value.find { it.id == child.financialAccountId }?.let { UnifiedAccount.Financial(it) } ?:
                                           parties.value.find { it.id == child.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                            }
                        }

                        CostRow(
                            type = child.linkedTransactionType ?: LinkedTransactionType.OTHER,
                            amount = child.amount.toPlainString(),
                            sourceAccount = childSrc,
                            destinationAccount = childDest,
                            note = child.note ?: ""
                        )
                    }

                    // Reconstruct source/destination for unified UI if applicable
                    when (tx.businessType) {
                        BusinessTransactionType.TRANSFER -> {
                            sourceAccount = financialAccounts.value.find { it.id == tx.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                            destinationAccount = financialAccounts.value.find { it.id == tx.toFinancialAccountId }?.let { UnifiedAccount.Financial(it) }
                        }
                        BusinessTransactionType.PARTY_SETTLEMENT -> {
                            sourceAccount = parties.value.find { it.id == tx.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                            destinationAccount = parties.value.find { it.id == tx.toPartyId }?.let { UnifiedAccount.PartyAccount(it) }
                        }
                        BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EQUITY_WITHDRAWAL -> {
                            sourceAccount = financialAccounts.value.find { it.id == tx.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                            destinationAccount = parties.value.find { it.id == tx.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                        }
                        BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> {
                            sourceAccount = parties.value.find { it.id == tx.partyId }?.let { UnifiedAccount.PartyAccount(it) }
                            destinationAccount = financialAccounts.value.find { it.id == tx.financialAccountId }?.let { UnifiedAccount.Financial(it) }
                        }
                        else -> { /* Not a unified transfer type */ }
                    }
                }
            }
        } else {
            isEditing = false
        }
    }

    val amount: BigDecimal
        get() = try {
            BigDecimal(amountText)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

    val totalAmount: BigDecimal
        get() = amount.add(additionalCosts.fold(BigDecimal.ZERO) { acc, cost ->
            acc.add(try { BigDecimal(cost.amount) } catch (e: Exception) { BigDecimal.ZERO })
        })

    val baseQuantity: BigDecimal?
        get() = try {
            val q = BigDecimal(quantity)
            val u = units.value.find { it.id == unitId }
            if (u != null) {
                q.multiply(u.multiplier)
            } else {
                q
            }
        } catch (e: Exception) {
            null
        }

    var isSubmitting by mutableStateOf(false)
        private set

    fun isFormValid(): Boolean {
        val isProductRequired = businessType == BusinessTransactionType.PURCHASE || 
                                businessType == BusinessTransactionType.SALE || 
                                businessType == BusinessTransactionType.STOCK_ADJUSTMENT
        
        if (isProductRequired) {
            if (businessType != BusinessTransactionType.STOCK_ADJUSTMENT && (partyId == null || partyId == 0L)) return false
            if (productId == null || productId == 0L) return false
            if (unitId == null || unitId == 0L) return false
            val q = try { BigDecimal(quantity) } catch(e: Exception) { BigDecimal.ZERO }
            if (q.compareTo(BigDecimal.ZERO) == 0) return false
            val a = try { BigDecimal(amountText) } catch(e: Exception) { BigDecimal.ZERO }
            if (a.compareTo(BigDecimal.ZERO) == 0) return false
        } else if (isTransferMode) {
            if (businessType == BusinessTransactionType.PARTY_SETTLEMENT) {
                if (partyId == null || partyId == 0L || toPartyId == null || toPartyId == 0L) return false
            } else if (sourceAccount == null || destinationAccount == null) return false
            val a = try { BigDecimal(amountText) } catch(e: Exception) { BigDecimal.ZERO }
            if (a.compareTo(BigDecimal.ZERO) == 0) return false
        } else if (businessType == BusinessTransactionType.EXPENSE) {
            if (expenseCategoryId == null || expenseCategoryId == 0L) return false
            if (financialAccountId == null || financialAccountId == 0L) return false
            val a = try { BigDecimal(amountText) } catch(e: Exception) { BigDecimal.ZERO }
            if (a.compareTo(BigDecimal.ZERO) == 0) return false
        } else if (businessType == BusinessTransactionType.PROFIT_DISTRIBUTION) {
            if (partyId == null || partyId == 0L) return false
            val a = try { BigDecimal(amountText) } catch(e: Exception) { BigDecimal.ZERO }
            if (a.compareTo(BigDecimal.ZERO) == 0) return false
        }

        return true
    }

    fun submitTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isFormValid()) {
            showErrors = true
            onError("Please fill all required fields correctly.")
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            val isProductRequired = businessType == BusinessTransactionType.PURCHASE || 
                                    businessType == BusinessTransactionType.SALE || 
                                    businessType == BusinessTransactionType.STOCK_ADJUSTMENT

            val result = addTransactionUseCase(
                transactionId = transactionId,
                partyId = partyId,
                toPartyId = toPartyId,
                productId = productId,
                unitId = unitId,
                financialAccountId = financialAccountId,
                toFinancialAccountId = toFinancialAccountId,
                expenseCategoryId = expenseCategoryId,
                quantity = if (isProductRequired) try { BigDecimal(quantity) } catch(e: Exception) { null } else null,
                baseQuantity = if (isProductRequired) baseQuantity else null,
                rate = if (isProductRequired) try { BigDecimal(rate) } catch(e: Exception) { null } else null,
                amount = amount,
                businessType = businessType,
                note = note.takeIf { it.isNotBlank() },
                additionalCosts = additionalCosts.map { cost ->
                    com.sabid.khotianv2.domain.usecase.AdditionalCost(
                        type = cost.type,
                        amount = try { BigDecimal(cost.amount) } catch (e: Exception) { BigDecimal.ZERO },
                        partyId = if (cost.sourceAccount is UnifiedAccount.PartyAccount) cost.sourceAccount.id else null,
                        toPartyId = if (cost.destinationAccount is UnifiedAccount.PartyAccount) cost.destinationAccount.id else null,
                        financialAccountId = if (cost.sourceAccount is UnifiedAccount.Financial) cost.sourceAccount.id else null,
                        toFinancialAccountId = if (cost.destinationAccount is UnifiedAccount.Financial) cost.destinationAccount.id else null,
                        note = cost.note.takeIf { it.isNotBlank() }
                    )
                }
            )
            isSubmitting = false
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Unknown error") }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("partyId") initialPartyId: Long?,
            @Assisted("transactionId") transactionId: Long?
        ): TransactionEntryViewModel
    }
}
