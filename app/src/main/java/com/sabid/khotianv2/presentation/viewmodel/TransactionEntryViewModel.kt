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
    @Assisted private val initialPartyId: Long?,
    private val businessRepository: BusinessRepository,
    private val productRepository: ProductRepository,
    private val unitRepository: UnitRepository,
    private val financialAccountRepository: FinancialAccountRepository,
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

    val userPermissions: StateFlow<UserPermissions> = permissionManager.userPermissions

    var partyId by mutableStateOf<Long?>(initialPartyId)
    var productId by mutableStateOf<Long?>(null)
    var unitId by mutableStateOf<Long?>(null)
    var financialAccountId by mutableStateOf<Long?>(null)
    var toFinancialAccountId by mutableStateOf<Long?>(null)
    var quantity by mutableStateOf("0")
    var rate by mutableStateOf("0")
    var amountText by mutableStateOf("0")
    var freightAmountText by mutableStateOf("0")
    var freightType by mutableStateOf(FreightType.BORN_BY_SELLER)
    var businessType by mutableStateOf(BusinessTransactionType.SALE)
    var note by mutableStateOf("")

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
        get() = if (freightType == FreightType.BORN_BY_US) amount.add(freightAmount) else amount

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
            val result = addTransactionUseCase(
                partyId = partyId,
                productId = productId,
                unitId = unitId,
                financialAccountId = financialAccountId,
                toFinancialAccountId = toFinancialAccountId,
                quantity = if (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE) try { BigDecimal(quantity) } catch(e: Exception) { null } else null,
                baseQuantity = if (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE) baseQuantity else null,
                rate = if (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE) try { BigDecimal(rate) } catch(e: Exception) { null } else null,
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
        fun create(initialPartyId: Long?): TransactionEntryViewModel
    }
}
