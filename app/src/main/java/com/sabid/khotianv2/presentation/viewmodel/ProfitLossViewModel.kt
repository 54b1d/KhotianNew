package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.ProfitLossReport
import com.sabid.khotianv2.domain.usecase.GetProfitLossUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ProfitLossViewModel @Inject constructor(
    private val getProfitLossUseCase: GetProfitLossUseCase
) : ViewModel() {

    val report: StateFlow<ProfitLossReport> = getProfitLossUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfitLossReport(
                totalSales = BigDecimal.ZERO,
                totalPurchases = BigDecimal.ZERO,
                totalExpenses = BigDecimal.ZERO,
                openingStockValue = BigDecimal.ZERO,
                closingStockValue = BigDecimal.ZERO,
                costOfGoodsSold = BigDecimal.ZERO,
                grossProfit = BigDecimal.ZERO,
                expensesByCategory = emptyMap(),
                netProfit = BigDecimal.ZERO
            )
        )
}
