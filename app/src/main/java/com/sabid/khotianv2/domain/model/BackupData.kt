package com.sabid.khotianv2.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val units: List<AppUnit>,
    val products: List<Product>,
    val parties: List<Party>,
    val transactions: List<Transaction>,
    val financialAccounts: List<FinancialAccount> = emptyList()
)
