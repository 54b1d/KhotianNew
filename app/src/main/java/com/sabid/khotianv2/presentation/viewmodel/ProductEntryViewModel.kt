package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.Product
import com.sabid.khotianv2.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductEntryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductEntryState>(ProductEntryState.Idle)
    val uiState: StateFlow<ProductEntryState> = _uiState

    fun addProduct(name: String, unit: String, category: String?) {
        if (name.isBlank() || unit.isBlank()) {
            _uiState.value = ProductEntryState.Error("Name and Unit are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProductEntryState.Loading
            try {
                productRepository.addProduct(
                    Product(name = name, unit = unit, category = category)
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
