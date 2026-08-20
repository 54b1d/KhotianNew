package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.AppUnit
import com.sabid.khotianv2.domain.model.Product
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.repository.UnitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ProductEntryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductEntryState>(ProductEntryState.Idle)
    val uiState: StateFlow<ProductEntryState> = _uiState.asStateFlow()

    private val _units = MutableStateFlow<List<AppUnit>>(emptyList())
    val units: StateFlow<List<AppUnit>> = _units.asStateFlow()

    init {
        viewModelScope.launch {
            unitRepository.getAllUnits().collect {
                _units.value = it
            }
        }
    }

    fun addProduct(name: String, defaultUnitId: Long?, category: String?, openingBalance: String) {
        if (name.isBlank()) {
            _uiState.value = ProductEntryState.Error("Name is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProductEntryState.Loading
            try {
                productRepository.addProduct(
                    Product(
                        name = name,
                        defaultUnitId = defaultUnitId,
                        category = category,
                        openingBalance = openingBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    )
                )
                _uiState.value = ProductEntryState.Success
            } catch (e: Exception) {
                _uiState.value = ProductEntryState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = ProductEntryState.Idle
    }
}

sealed class ProductEntryState {
    object Idle : ProductEntryState()
    object Loading : ProductEntryState()
    object Success : ProductEntryState()
    data class Error(val message: String) : ProductEntryState()
}
