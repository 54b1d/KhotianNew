package com.sabid.khotianv2.data.local

import androidx.room.withTransaction
import com.sabid.khotianv2.data.local.entity.*
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.PermissionType
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleDataGenerator @Inject constructor(
    private val db: AppDatabase,
    private val sessionManager: SessionManager
) {
    suspend fun generateSampleData() {
        db.withTransaction {
            db.clearAllTables()

            // 1. Initial Setup (Permissions, Role, Admin User)
            val permissions = PermissionType.values().map { PermissionEntity(it) }
            db.userDao().insertPermissions(permissions)

            val adminRoleId = db.userDao().insertRole(RoleEntity(name = "Administrator"))
            db.userDao().updateRolePermissions(adminRoleId, PermissionType.values().toList())

            val adminUser = UserEntity(
                username = "Test Admin",
                passwordHash = HashUtils.sha256("1234"),
                roleId = adminRoleId
            )
            val adminId = db.userDao().insertUser(adminUser)
            sessionManager.startSession(adminId.toString())

            // 1.5 Expense Categories
            db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Rent"))
            db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Labor"))
            db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Utilities"))

            // 2. Units
            val unitKgId = db.unitDao().insertUnit(
                UnitEntity(name = "Kilogram", symbol = "kg", multiplier = BigDecimal.ONE)
            )
            val unitBag74Id = db.unitDao().insertUnit(
                UnitEntity(name = "Bag (74kg)", symbol = "bag74", multiplier = BigDecimal("74.0"))
            )

            // 3. Products
            val mustardSeedId = db.productDao().insertProduct(
                ProductEntity(name = "Mustard Seed", defaultUnitId = unitKgId)
            )
            val mustardOilId = db.productDao().insertProduct(
                ProductEntity(name = "Mustard Oil", defaultUnitId = unitKgId)
            )
            val oilCakeId = db.productDao().insertProduct(
                ProductEntity(name = "Oil Cake", defaultUnitId = unitKgId)
            )

            // 4. Financial Accounts
            val mainCashId = db.financialAccountDao().insertAccount(
                FinancialAccountEntity(
                    name = "Main Cash",
                    type = FinancialAccountType.CASH,
                    openingBalance = BigDecimal("10000.00"),
                    currentBalance = BigDecimal("10000.00")
                )
            )
            val businessBankId = db.financialAccountDao().insertAccount(
                FinancialAccountEntity(
                    name = "Business Bank",
                    type = FinancialAccountType.BANK,
                    openingBalance = BigDecimal("50000.00"),
                    currentBalance = BigDecimal("50000.00")
                )
            )

            // 5. Parties
            val supplierAlphaId = db.partyDao().insertParty(
                PartyEntity(
                    name = "Seed Supplier Alpha",
                    phoneNumber = "0123456789",
                    address = "Seed Market",
                    type = "SUPPLIER"
                )
            )
            val buyerBetaId = db.partyDao().insertParty(
                PartyEntity(
                    name = "Oil Buyer Beta",
                    phoneNumber = "0987654321",
                    address = "City Center",
                    type = "CUSTOMER"
                )
            )

            // 6. Sample Transactions
            val creatorId = adminId.toString()

            // A purchase of seeds: 1000kg at 80/kg = 80,000. Born by seller.
            val purchaseAmount = BigDecimal("80000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = supplierAlphaId,
                    productId = mustardSeedId,
                    unitId = unitKgId,
                    quantity = BigDecimal("1000"),
                    baseQuantity = BigDecimal("1000"),
                    rate = BigDecimal("80"),
                    amount = purchaseAmount,
                    type = TransactionType.CREDIT,
                    businessType = BusinessTransactionType.PURCHASE,
                    note = "Initial seed stock purchase",
                    createdBy = creatorId
                )
            )

            // A sale of oil: 500kg at 180/kg = 90,000.
            val saleAmount = BigDecimal("90000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = buyerBetaId,
                    productId = mustardOilId,
                    unitId = unitKgId,
                    quantity = BigDecimal("500"),
                    baseQuantity = BigDecimal("500"),
                    rate = BigDecimal("180"),
                    amount = saleAmount,
                    type = TransactionType.DEBIT,
                    businessType = BusinessTransactionType.SALE,
                    note = "First oil sale",
                    createdBy = creatorId
                )
            )

            // A payment to a supplier: 50,000 from Business Bank
            val paymentAmount = BigDecimal("50000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = supplierAlphaId,
                    financialAccountId = businessBankId,
                    amount = paymentAmount,
                    type = TransactionType.DEBIT, // Debit party (reduce payable)
                    businessType = BusinessTransactionType.PAYMENT_MADE,
                    note = "Partial payment for seeds",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(businessBankId, paymentAmount.negate())

            // A bank-to-cash transfer: 5,000 from Bank to Cash
            val transferAmount = BigDecimal("5000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    financialAccountId = businessBankId,
                    toFinancialAccountId = mainCashId,
                    amount = transferAmount,
                    type = TransactionType.TRANSFER,
                    businessType = BusinessTransactionType.TRANSFER,
                    note = "Cash withdrawal for expenses",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().transferBalance(businessBankId, mainCashId, transferAmount)
        }
    }
}
