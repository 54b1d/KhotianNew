package com.sabid.khotianv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
@Entity(tableName = "parties")
data class PartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String?,
    val address: String?,
    val type: String // e.g., CUSTOMER, SUPPLIER, BOTH
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String, // e.g., Kg, Liter, Bag
    val category: String? = null
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = PartyEntity::class,
            parentColumns = ["id"],
            childColumns = ["partyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long,
    val productId: Long? = null,
    val quantity: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val amount: BigDecimal, // Base amount (qty * rate or direct amount)
    val freightAmount: BigDecimal = BigDecimal.ZERO,
    val freightType: FreightType = FreightType.BORN_BY_SELLER,
    val netCost: BigDecimal = BigDecimal.ZERO,
    val type: TransactionType,
    val businessType: BusinessTransactionType,
    val note: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String // UserId
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
@Entity(tableName = "crushing_batches")
data class CrushingBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchNumber: String,
    val seedType: String,
    val seedQuantity: BigDecimal, // Kg
    val seedRate: BigDecimal = BigDecimal.ZERO,
    val oilQuantity: BigDecimal, // Liter/Kg
    val oilCakeQuantity: BigDecimal, // Kg
    val wasteQuantity: BigDecimal = BigDecimal.ZERO,
    val crushingCharge: BigDecimal = BigDecimal.ZERO,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String?
)
