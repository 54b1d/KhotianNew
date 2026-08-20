package com.sabid.khotianv2.domain.model

import java.math.BigDecimal
import java.time.LocalDate

data class HomeData(
    val selectedDate: LocalDate,
    val transactions: List<TransactionItem>,
    val cashAccounts: List<FinancialAccount>,
    val bankAccounts: List<FinancialAccount>,
    val cashTotal: BigDecimal,
    val bankTotal: BigDecimal
)

data class TransactionItem(
    val transaction: Transaction,
    val partyName: String? = null,
    val accountName: String? = null,
    val toAccountName: String? = null,
    val categoryName: String? = null,
    val unitName: String? = null,
    val productName: String? = null
)
