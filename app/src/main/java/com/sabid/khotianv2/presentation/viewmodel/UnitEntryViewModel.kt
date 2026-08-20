package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.AppUnit
import com.sabid.khotianv2.domain.repository.UnitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class UnitEntryViewModel @Inject constructor(
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _units = MutableStateFlow<List<AppUnit>>(emptyList())
    val units: StateFlow<List<AppUnit>> = _units.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _symbol = MutableStateFlow("")
    val symbol: StateFlow<String> = _symbol.asStateFlow()

    private val _multiplier = MutableStateFlow("")
    val multiplier: StateFlow<String> = _multiplier.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUnits()
    }

    private fun loadUnits() {
        viewModelScope.launch {
            unitRepository.getAllUnits().collect {
                _units.value = it
            }
        }
    }

    fun onNameChange(newName: String) {
        _name.value = newName
    }

    fun onSymbolChange(newSymbol: String) {
        _symbol.value = newSymbol
    }

    fun onMultiplierChange(newMultiplier: String) {
        _multiplier.value = newMultiplier
    }

    fun addUnit() {
        val multiplierVal = _multiplier.value.toBigDecimalOrNull() ?: return
        val nameVal = _name.value
        val symbolVal = _symbol.value

        if (nameVal.isBlank() || symbolVal.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            unitRepository.addUnit(
                AppUnit(
                    name = nameVal,
                    symbol = symbolVal,
                    multiplier = multiplierVal
                )
            )
            _name.value = ""
            _symbol.value = ""
            _multiplier.value = ""
            _isLoading.value = false
        }
    }

    fun deleteUnit(unit: AppUnit) {
        viewModelScope.launch {
            unitRepository.deleteUnit(unit)
        }
    }
}
