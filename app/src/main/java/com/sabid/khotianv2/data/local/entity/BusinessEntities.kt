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
    val type: String, // e.g., CUSTOMER, SUPPLIER, BOTH
    val openingBalance: BigDecimal = BigDecimal.ZERO
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val symbol: String,
    val multiplier: BigDecimal
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultUnitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultUnitId: Long? = null,
    val category: String? = null,
    val openingBalance: BigDecimal = BigDecimal.ZERO
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "financial_accounts")
data class FinancialAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: FinancialAccountType,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    val currentBalance: BigDecimal = BigDecimal.ZERO
)

enum class FinancialAccountType {
    CASH, BANK
}

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
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FinancialAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["financialAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FinancialAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toFinancialAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ExpenseCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseCategoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long? = null,
    val productId: Long? = null,
    val unitId: Long? = null,
    val financialAccountId: Long? = null,
    val toFinancialAccountId: Long? = null,
    val expenseCategoryId: Long? = null,
    val quantity: BigDecimal? = null,
    val baseQuantity: BigDecimal? = null,
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
    DEBIT, CREDIT, TRANSFER, EXPENSE, STOCK_ADJUSTMENT
}

enum class BusinessTransactionType {
    PURCHASE, SALE, PAYMENT_MADE, PAYMENT_RECEIVED, TRANSFER, EXPENSE, STOCK_ADJUSTMENT
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

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "stocktakes",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StocktakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val physicalQuantity: BigDecimal,
    val unitPrice: BigDecimal,
    val timestamp: Long = System.currentTimeMillis()
)
