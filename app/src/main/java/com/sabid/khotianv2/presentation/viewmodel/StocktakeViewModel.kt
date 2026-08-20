package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.Stocktake
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.repository.ProductStock
import com.sabid.khotianv2.domain.repository.StocktakeRepository
import com.sabid.khotianv2.domain.usecase.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class StocktakeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stocktakeRepository: StocktakeRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    private val _stockStates = MutableStateFlow<List<ProductStocktakeState>>(emptyList())
    val stockStates: StateFlow<List<ProductStocktakeState>> = _stockStates.asStateFlow()

    private val _isAdjustToggleOn = MutableStateFlow(true)
    val isAdjustToggleOn: StateFlow<Boolean> = _isAdjustToggleOn.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.getProductStock().collect { stocks ->
                _stockStates.value = stocks.map { stock ->
                    ProductStocktakeState(
                        productId = stock.productId,
                        productName = stock.productName,
                        systemStock = stock.baseStock,
                        physicalQuantity = "",
                        unitPrice = ""
                    )
                }
            }
        }
    }

    fun onPhysicalQuantityChange(productId: Long, value: String) {
        _stockStates.value = _stockStates.value.map {
            if (it.productId == productId) it.copy(physicalQuantity = value) else it
        }
    }

    fun onUnitPriceChange(productId: Long, value: String) {
        _stockStates.value = _stockStates.value.map {
            if (it.productId == productId) it.copy(unitPrice = value) else it
        }
    }

    fun onToggleAdjust(value: Boolean) {
        _isAdjustToggleOn.value = value
    }

    fun saveStocktake(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                _stockStates.value.forEach { state ->
                    val physicalQty = state.physicalQuantity.toBigDecimalOrNull()
                    val price = state.unitPrice.toBigDecimalOrNull()

                    if (physicalQty != null && price != null) {
                        // 1. Save Stocktake
                        val stocktake = Stocktake(
                            productId = state.productId,
                            physicalQuantity = physicalQty,
                            unitPrice = price
                        )
                        stocktakeRepository.addStocktake(stocktake)

                        // 2. Adjust Stock Balance if toggle is ON
                        if (_isAdjustToggleOn.value) {
                            val difference = physicalQty.subtract(state.systemStock)
                            if (difference != BigDecimal.ZERO) {
                                addTransactionUseCase(
                                    productId = state.productId,
                                    baseQuantity = difference,
                                    amount = difference.abs().multiply(price),
                                    businessType = BusinessTransactionType.STOCK_ADJUSTMENT,
                                    note = if (difference < BigDecimal.ZERO) "Stock Write-off" else "Stock Addition"
                                )
                            }
                        }
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}

data class ProductStocktakeState(
    val productId: Long,
    val productName: String,
    val systemStock: BigDecimal,
    val physicalQuantity: String,
    val unitPrice: String
)