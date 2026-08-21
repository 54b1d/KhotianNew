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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

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
    var freightAmountText by mutableStateOf("0")
    var freightType by mutableStateOf(FreightType.BORN_BY_SELLER)
    var businessType by mutableStateOf(BusinessTransactionType.SALE)
    var note by mutableStateOf("")

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
        freightAmountText = "0"
        freightType = FreightType.BORN_BY_SELLER
        businessType = BusinessTransactionType.SALE
        note = ""
        
        if (id != null) {
            isEditing = true
            viewModelScope.launch {
                val tx = transactionRepository.getTransactionById(id)
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
                    freightAmountText = tx.freightAmount.toPlainString()
                    freightType = tx.freightType
                    businessType = tx.businessType
                    note = tx.note ?: ""
                }
            }
        } else {
            isEditing = false
        }
    }

    val amount: BigDecimal
        get() = try {
            if (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE) {
                val q = BigDecimal(quantity)
                val r = BigDecimal(rate)
                q.multiply(r)
            } else {
                BigDecimal(amountText)
            }
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

    val freightAmount: BigDecimal
        get() = try {
            BigDecimal(freightAmountText)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }

    val netCost: BigDecimal
        get() = if (freightType == FreightType.BORN_BY_US && (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE)) {
            amount.add(freightAmount)
        } else {
            amount
        }

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

    fun submitTransaction(onSuccess: () -> Unit, onError: (String) -> Unit) {
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
                freightAmount = freightAmount,
                freightType = freightType,
                businessType = businessType,
                note = note.takeIf { it.isNotBlank() }
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
