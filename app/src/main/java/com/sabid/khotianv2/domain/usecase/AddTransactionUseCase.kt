package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import java.math.BigDecimal
import javax.inject.Inject

data class AdditionalCost(
    val type: LinkedTransactionType,
    val amount: BigDecimal,
    val partyId: Long? = null,
    val toPartyId: Long? = null,
    val financialAccountId: Long? = null,
    val toFinancialAccountId: Long? = null,
    val note: String? = null
)

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val financialAccountRepository: FinancialAccountRepository,
    private val permissionManager: PermissionManager,
    private val sessionManager: SessionManager,
) {
    suspend operator fun invoke(
        transactionId: Long? = null,
        partyId: Long? = null,
        toPartyId: Long? = null,
        productId: Long? = null,
        unitId: Long? = null,
        financialAccountId: Long? = null,
        toFinancialAccountId: Long? = null,
        expenseCategoryId: Long? = null,
        quantity: BigDecimal? = null,
        baseQuantity: BigDecimal? = null,
        rate: BigDecimal? = null,
        amount: BigDecimal,
        businessType: BusinessTransactionType,
        note: String?,
        additionalCosts: List<AdditionalCost> = emptyList()
    ): Result<Long> {
        if (!permissionManager.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
            return Result.failure(Exception("Permission denied"))
        }

        // 1. Fetch OLD transaction and its children if editing
        val oldTransaction = transactionId?.let { transactionRepository.getTransactionById(it) }
        val oldChildren = transactionId?.let { transactionRepository.getChildTransactions(it) } ?: emptyList()

        // ... (validation remains similar for main transaction)
        
        // Basic validation for main transaction
        if (businessType == BusinessTransactionType.TRANSFER) {
            if ((financialAccountId == null) || (toFinancialAccountId == null)) {
                return Result.failure(Exception("Both source and destination accounts must be selected for transfer"))
            }
        } else if (businessType == BusinessTransactionType.EXPENSE) {
            if (expenseCategoryId == null) return Result.failure(Exception("Expense category must be selected"))
            if (financialAccountId == null) return Result.failure(Exception("Payment account must be selected"))
        }

        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("User not authenticated"))

        // Mapping Business Type for main transaction
        val type = when (businessType) {
            BusinessTransactionType.SALE, BusinessTransactionType.PAYMENT_MADE -> TransactionType.DEBIT
            BusinessTransactionType.PURCHASE, BusinessTransactionType.PAYMENT_RECEIVED -> TransactionType.CREDIT
            BusinessTransactionType.EQUITY_WITHDRAWAL -> TransactionType.EQUITY
            BusinessTransactionType.EQUITY_CONTRIBUTION, BusinessTransactionType.PROFIT_DISTRIBUTION -> TransactionType.EQUITY
            BusinessTransactionType.TRANSFER -> TransactionType.TRANSFER
            BusinessTransactionType.EXPENSE -> TransactionType.EXPENSE
            BusinessTransactionType.STOCK_ADJUSTMENT -> TransactionType.STOCK_ADJUSTMENT
            BusinessTransactionType.PARTY_SETTLEMENT -> TransactionType.PARTY_SETTLEMENT
        }

        // 2. Reverse effects of OLD transaction and its children
        suspend fun reverseTransactionEffects(tx: Transaction) {
            if (tx.businessType == BusinessTransactionType.TRANSFER) {
                if (tx.financialAccountId != null && tx.toFinancialAccountId != null) {
                    financialAccountRepository.transferBalance(tx.toFinancialAccountId!!, tx.financialAccountId!!, tx.amount)
                }
            } else if (tx.financialAccountId != null) {
                val balanceChange = when (tx.businessType) {
                    BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> tx.amount
                    BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE,
                    BusinessTransactionType.EQUITY_WITHDRAWAL,
                    BusinessTransactionType.PROFIT_DISTRIBUTION -> tx.amount.negate()
                    else -> BigDecimal.ZERO
                }
                if (balanceChange != BigDecimal.ZERO) {
                    financialAccountRepository.updateBalance(tx.financialAccountId!!, balanceChange.negate())
                }
            }
        }

        oldTransaction?.let { reverseTransactionEffects(it) }
        oldChildren.forEach { reverseTransactionEffects(it) }

        // 3. Save / Update Main Transaction
        val transaction = Transaction(
            id = transactionId ?: 0,
            partyId = partyId,
            toPartyId = toPartyId,
            productId = productId,
            unitId = unitId,
            financialAccountId = financialAccountId,
            toFinancialAccountId = toFinancialAccountId,
            expenseCategoryId = expenseCategoryId,
            quantity = quantity,
            baseQuantity = baseQuantity,
            rate = rate,
            amount = amount,
            type = type,
            businessType = businessType,
            note = note,
            createdBy = userId,
            timestamp = oldTransaction?.timestamp ?: System.currentTimeMillis()
        )

        val finalTransactionId = if (transactionId != null) {
            transactionRepository.updateTransaction(transaction)
            // Delete old children
            transactionRepository.deleteChildTransactions(transactionId)
            transactionId
        } else {
            transactionRepository.addTransaction(transaction)
        }

        // 4. Apply NEW effects for main transaction
        suspend fun applyTransactionEffects(tx: Transaction) {
            if (tx.businessType == BusinessTransactionType.TRANSFER) {
                if (tx.financialAccountId != null && tx.toFinancialAccountId != null) {
                    financialAccountRepository.transferBalance(tx.financialAccountId!!, tx.toFinancialAccountId!!, tx.amount)
                }
            } else if (tx.financialAccountId != null) {
                val balanceChange = when (tx.businessType) {
                    BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> tx.amount
                    BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE,
                    BusinessTransactionType.EQUITY_WITHDRAWAL, BusinessTransactionType.PROFIT_DISTRIBUTION -> tx.amount.negate()
                    else -> BigDecimal.ZERO
                }
                if (balanceChange != BigDecimal.ZERO) {
                    financialAccountRepository.updateBalance(tx.financialAccountId!!, balanceChange)
                }
            }
        }

        applyTransactionEffects(transaction)

        // 5. Handle Additional Costs
        additionalCosts.forEach { cost ->
            // Flexible child transaction logic
            val childBusinessType: BusinessTransactionType
            val childType: TransactionType
            
            val payerIsFinancial = cost.financialAccountId != null
            val payerIsParty = cost.partyId != null
            val payeeIsFinancial = cost.toFinancialAccountId != null
            val payeeIsParty = cost.toPartyId != null

            when {
                // Financial Payer + Empty Payee -> Direct Cash Expense
                payerIsFinancial && !payeeIsFinancial && !payeeIsParty -> {
                    childBusinessType = BusinessTransactionType.EXPENSE
                    childType = TransactionType.EXPENSE
                }
                // Empty Payer + Party Payee -> Payable (Accrual)
                !payerIsFinancial && !payerIsParty && payeeIsParty -> {
                    childBusinessType = BusinessTransactionType.PURCHASE // Service purchase
                    childType = TransactionType.CREDIT
                }
                // Financial Payer + Party Payee -> Paid on behalf of Party
                payerIsFinancial && payeeIsParty -> {
                    childBusinessType = BusinessTransactionType.PAYMENT_MADE
                    childType = TransactionType.DEBIT
                }
                // Party Payer + Financial Payee -> Party paid us
                payerIsParty && payeeIsFinancial -> {
                    childBusinessType = BusinessTransactionType.PAYMENT_RECEIVED
                    childType = TransactionType.CREDIT
                }
                // Financial Payer + Financial Payee -> Internal Transfer
                payerIsFinancial && payeeIsFinancial -> {
                    childBusinessType = BusinessTransactionType.TRANSFER
                    childType = TransactionType.TRANSFER
                }
                // Party Payer + Party Payee -> Settlement
                payerIsParty && payeeIsParty -> {
                    childBusinessType = BusinessTransactionType.PARTY_SETTLEMENT
                    childType = TransactionType.PARTY_SETTLEMENT
                }
                // Other combinations default to Expense if possible or Purchase
                payerIsFinancial -> {
                    childBusinessType = BusinessTransactionType.EXPENSE
                    childType = TransactionType.EXPENSE
                }
                else -> {
                    childBusinessType = BusinessTransactionType.PURCHASE
                    childType = TransactionType.CREDIT
                }
            }

            val childTx = Transaction(
                parentTransactionId = finalTransactionId,
                linkedTransactionType = cost.type,
                partyId = when {
                    payerIsParty -> cost.partyId
                    payeeIsParty -> cost.toPartyId
                    else -> null
                },
                toPartyId = if (payerIsParty && payeeIsParty) cost.toPartyId else null,
                financialAccountId = when {
                    payerIsFinancial -> cost.financialAccountId
                    payeeIsFinancial -> cost.toFinancialAccountId
                    else -> null
                },
                toFinancialAccountId = if (payerIsFinancial && payeeIsFinancial) cost.toFinancialAccountId else null,
                amount = cost.amount,
                type = childType,
                businessType = childBusinessType,
                note = cost.note ?: "Linked ${cost.type}",
                createdBy = userId,
                timestamp = transaction.timestamp
            )
            
            transactionRepository.addTransaction(childTx)
            applyTransactionEffects(childTx)
        }

        return Result.success(finalTransactionId)
    }
}
