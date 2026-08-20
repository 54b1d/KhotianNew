package com.sabid.khotianv2.domain.model

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class Party(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val type: String
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: Long = 0,
    val name: String,
    val unit: String,
    val category: String? = null
)

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: Long = 0,
    val partyId: Long,
    val productId: Long? = null,
    val quantity: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val amount: BigDecimal, // Base amount
    val freightAmount: BigDecimal = BigDecimal.ZERO,
    val freightType: FreightType = FreightType.BORN_BY_SELLER,
    val netCost: BigDecimal = BigDecimal.ZERO,
    val type: TransactionType,
    val businessType: BusinessTransactionType,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String
)

enum class TransactionType {
    DEBIT, CREDIT
}

enum class BusinessTransactionType {
    PURCHASE, SALE, PAYMENT_MADE, PAYMENT_RECEIVED
}

enum class FreightType {
    BORN_BY_US, BORN_BY_SELLER
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
