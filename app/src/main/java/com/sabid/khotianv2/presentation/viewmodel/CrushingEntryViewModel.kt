package com.sabid.khotianv2.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.usecase.ProcessCrushingBatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CrushingEntryViewModel @Inject constructor(
    private val processCrushingBatchUseCase: ProcessCrushingBatchUseCase
) : ViewModel() {

    var batchNumber by mutableStateOf("")
    var seedType by mutableStateOf("")
    var seedQuantity by mutableStateOf("")
    var seedRate by mutableStateOf("")
    var oilQuantity by mutableStateOf("")
    var oilCakeQuantity by mutableStateOf("")
    var wasteQuantity by mutableStateOf("")
    var crushingCharge by mutableStateOf("")
    var note by mutableStateOf("")

    var isSubmitting by mutableStateOf(false)
        private set

    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isSubmitting = true
            val result = processCrushingBatchUseCase(
                batchNumber = batchNumber,
                seedType = seedType,
                seedQuantity = seedQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                seedRate = seedRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                oilQuantity = oilQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                oilCakeQuantity = oilCakeQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                wasteQuantity = wasteQuantity.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                crushingCharge = crushingCharge.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                note = note.takeIf { it.isNotBlank() }
            )
            isSubmitting = false
            result.onSuccess { onSuccess() }.onFailure { onError(it.message ?: "Unknown error") }
        }
    }
}
