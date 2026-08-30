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
            val rentCategoryId = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Rent"))
            val laborCategoryId = db.expenseCategoryDao().insertCategory(ExpenseCategoryEntity(name = "Labor"))
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
            val bkashId = db.financialAccountDao().insertAccount(
                FinancialAccountEntity(
                    name = "bKash (Merchant)",
                    type = FinancialAccountType.BANK,
                    openingBalance = BigDecimal("5000.00"),
                    currentBalance = BigDecimal("5000.00")
                )
            )

            // 5. Parties
            val supplierAlphaId = db.partyDao().insertParty(
                PartyEntity(
                    name = "Seed Supplier Alpha",
                    phoneNumber = "0123456789",
                    address = "Seed Market",
                    type = "SUPPLIER",
                    openingBalance = BigDecimal.ZERO,
                    currentBalance = BigDecimal.ZERO
                )
            )
            val buyerBetaId = db.partyDao().insertParty(
                PartyEntity(
                    name = "Oil Buyer Beta",
                    phoneNumber = "0987654321",
                    address = "City Center",
                    type = "CUSTOMER",
                    openingBalance = BigDecimal.ZERO,
                    currentBalance = BigDecimal.ZERO
                )
            )
            val investorGammaId = db.partyDao().insertParty(
                PartyEntity(
                    name = "Investor Gamma",
                    phoneNumber = "0111222333",
                    address = "Wealth District",
                    type = "INVESTOR",
                    openingBalance = BigDecimal.ZERO,
                    currentBalance = BigDecimal.ZERO
                )
            )

            // 6. Sample Transactions
            val creatorId = adminId.toString()

            // A. Equity Contribution: 100,000 from Investor Gamma to Bank
            val equityAmount = BigDecimal("100000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = investorGammaId,
                    financialAccountId = businessBankId,
                    amount = equityAmount,
                    type = TransactionType.EQUITY,
                    businessType = BusinessTransactionType.EQUITY_CONTRIBUTION,
                    note = "Initial capital injection",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(businessBankId, equityAmount)
            db.partyDao().updateBalance(investorGammaId, equityAmount.negate())

            // B. Purchase of seeds: 1000kg at 80/kg = 80,000.
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
                    note = "Bulk seed stock purchase",
                    createdBy = creatorId
                )
            )
            db.partyDao().updateBalance(supplierAlphaId, purchaseAmount.negate())

            // C. Sale of oil: 500kg at 180/kg = 90,000.
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
                    note = "Major oil sale to retail distributor",
                    createdBy = creatorId
                )
            )
            db.partyDao().updateBalance(buyerBetaId, saleAmount)

            // D. Payment to supplier: 50,000 from Business Bank
            val paymentMadeAmount = BigDecimal("50000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = supplierAlphaId,
                    financialAccountId = businessBankId,
                    amount = paymentMadeAmount,
                    type = TransactionType.DEBIT,
                    businessType = BusinessTransactionType.PAYMENT_MADE,
                    note = "Partial payment for seed invoice",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(businessBankId, paymentMadeAmount.negate())
            db.partyDao().updateBalance(supplierAlphaId, paymentMadeAmount)

            // E. Payment received from customer: 40,000 into bKash
            val paymentReceivedAmount = BigDecimal("40000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    partyId = buyerBetaId,
                    financialAccountId = bkashId,
                    amount = paymentReceivedAmount,
                    type = TransactionType.CREDIT,
                    businessType = BusinessTransactionType.PAYMENT_RECEIVED,
                    note = "Downpayment via mobile banking",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(bkashId, paymentReceivedAmount)
            db.partyDao().updateBalance(buyerBetaId, paymentReceivedAmount.negate())

            // F. Expense: Rent 12,000 from Business Bank
            val rentAmount = BigDecimal("12000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    expenseCategoryId = rentCategoryId,
                    financialAccountId = businessBankId,
                    amount = rentAmount,
                    type = TransactionType.EXPENSE,
                    businessType = BusinessTransactionType.EXPENSE,
                    note = "Monthly warehouse rent",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(businessBankId, rentAmount.negate())

            // G. Bank-to-Cash transfer: 10,000 from Bank to Cash
            val bankToCashAmount = BigDecimal("10000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    financialAccountId = businessBankId,
                    toFinancialAccountId = mainCashId,
                    amount = bankToCashAmount,
                    type = TransactionType.TRANSFER,
                    businessType = BusinessTransactionType.TRANSFER,
                    note = "Cash withdrawal for daily operations",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().transferBalance(businessBankId, mainCashId, bankToCashAmount)

            // H. Bank-to-bKash transfer: 5,000 from Bank to bKash
            val bankToBkashAmount = BigDecimal("5000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    financialAccountId = businessBankId,
                    toFinancialAccountId = bkashId,
                    amount = bankToBkashAmount,
                    type = TransactionType.TRANSFER,
                    businessType = BusinessTransactionType.TRANSFER,
                    note = "Replenishing mobile money float",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().transferBalance(businessBankId, bkashId, bankToBkashAmount)

            // I. Stock Adjustment: -5kg Mustard Seed (Wastage)
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    productId = mustardSeedId,
                    unitId = unitKgId,
                    quantity = BigDecimal("-5"),
                    baseQuantity = BigDecimal("-5"),
                    amount = BigDecimal.ZERO,
                    type = TransactionType.STOCK_ADJUSTMENT,
                    businessType = BusinessTransactionType.STOCK_ADJUSTMENT,
                    note = "Seed wastage during handling",
                    createdBy = creatorId
                )
            )

            // J. Profit Distribution: 2,000 from Cash
            val profitDistAmount = BigDecimal("2000.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    financialAccountId = mainCashId,
                    amount = profitDistAmount,
                    type = TransactionType.EQUITY,
                    businessType = BusinessTransactionType.PROFIT_DISTRIBUTION,
                    note = "Owner dividend withdrawal",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(mainCashId, profitDistAmount.negate())

            // K. Labor Expense: 3,500 from Cash
            val laborAmount = BigDecimal("3500.00")
            db.transactionDao().insertTransaction(
                TransactionEntity(
                    expenseCategoryId = laborCategoryId,
                    financialAccountId = mainCashId,
                    amount = laborAmount,
                    type = TransactionType.EXPENSE,
                    businessType = BusinessTransactionType.EXPENSE,
                    note = "Daily labor payment",
                    createdBy = creatorId
                )
            )
            db.financialAccountDao().updateBalance(mainCashId, laborAmount.negate())
        }
    }
}
