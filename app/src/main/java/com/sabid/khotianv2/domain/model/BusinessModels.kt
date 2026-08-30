package com.sabid.khotianv2.domain.model

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class Party(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val type: String,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    val currentBalance: BigDecimal = BigDecimal.ZERO
)

@JsonClass(generateAdapter = true)
data class ExpenseCategory(
    val id: Long = 0,
    val name: String
)

@JsonClass(generateAdapter = true)
data class AppUnit(
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val multiplier: BigDecimal
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Long = 0,
    val name: String,
    val defaultUnitId: Long? = null,
    val category: String? = null,
    val openingBalance: BigDecimal = BigDecimal.ZERO
)

@JsonClass(generateAdapter = true)
data class FinancialAccount(
    val id: Long = 0,
    val name: String,
    val type: FinancialAccountType,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    val currentBalance: BigDecimal = BigDecimal.ZERO
)

enum class FinancialAccountType {
    CASH, BANK
}

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: Long = 0,
    val partyId: Long? = null,
    val toPartyId: Long? = null,
    val productId: Long? = null,
    val unitId: Long? = null,
    val financialAccountId: Long? = null,
    val toFinancialAccountId: Long? = null,
    val expenseCategoryId: Long? = null,
    val quantity: BigDecimal? = null,
    val baseQuantity: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val amount: BigDecimal, // Base amount
    val parentTransactionId: Long? = null,
    val linkedTransactionType: LinkedTransactionType? = null,
    val type: TransactionType,
    val businessType: BusinessTransactionType,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String
)

enum class TransactionType {
    DEBIT, CREDIT, TRANSFER, EXPENSE, STOCK_ADJUSTMENT, PARTY_SETTLEMENT, EQUITY
}

enum class BusinessTransactionType {
    PURCHASE, SALE, PAYMENT_MADE, PAYMENT_RECEIVED, TRANSFER, EXPENSE, STOCK_ADJUSTMENT, PARTY_SETTLEMENT,
    EQUITY_CONTRIBUTION, EQUITY_WITHDRAWAL, PROFIT_DISTRIBUTION
}

enum class LinkedTransactionType {
    FREIGHT, COMMISSION, LABOR, DISCOUNT, OTHER
}

@JsonClass(generateAdapter = true)
data class CrushingBatch(
    val id: Long = 0,
    val batchNumber: String,
    val seedType: String,
    val seedQuantity: BigDecimal,
    val seedRate: BigDecimal = BigDecimal.ZERO,
    val oilQuantity: BigDecimal,
    val oilCakeQuantity: BigDecimal,
    val wasteQuantity: BigDecimal = BigDecimal.ZERO,
    val crushingCharge: BigDecimal = BigDecimal.ZERO,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class AuditLog(
    val id: Long = 0,
    val tableName: String,
    val recordId: Long,
    val action: AuditAction,
    val oldValues: String?,
    val newValues: String?,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AuditAction {
    INSERT, UPDATE, DELETE
}

data class ProfitLossReport(
    val totalSales: BigDecimal,
    val totalPurchases: BigDecimal,
    val totalExpenses: BigDecimal,
    val openingStockValue: BigDecimal,
    val closingStockValue: BigDecimal,
    val costOfGoodsSold: BigDecimal,
    val grossProfit: BigDecimal,
    val expensesByCategory: Map<String, BigDecimal>,
    val netProfit: BigDecimal
)

@JsonClass(generateAdapter = true)
data class Stocktake(
    val id: Long = 0,
    val productId: Long,
    val physicalQuantity: BigDecimal,
    val unitPrice: BigDecimal,
    val timestamp: Long = System.currentTimeMillis()
)
