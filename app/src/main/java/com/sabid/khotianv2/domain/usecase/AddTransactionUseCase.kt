package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import java.math.BigDecimal
import javax.inject.Inject

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
        freightAmount: BigDecimal = BigDecimal.ZERO,
        freightType: FreightType = FreightType.BORN_BY_SELLER,
        businessType: BusinessTransactionType,
        note: String?,
    ): Result<Long> {
        if (!permissionManager.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
            return Result.failure(Exception("Permission denied"))
        }

        // 1. Fetch OLD transaction if editing
        val oldTransaction = transactionId?.let { transactionRepository.getTransactionById(it) }

        // Validation
        if (businessType == BusinessTransactionType.TRANSFER) {
            if ((financialAccountId == null) || (toFinancialAccountId == null)) {
                return Result.failure(Exception("Both source and destination accounts must be selected for transfer"))
            }
            if (financialAccountId == toFinancialAccountId) {
                return Result.failure(Exception("Source and destination accounts must be different"))
            }
        } else if (businessType == BusinessTransactionType.EXPENSE) {
            if (expenseCategoryId == null) {
                return Result.failure(Exception("Expense category must be selected"))
            }
            if (financialAccountId == null) {
                return Result.failure(Exception("Payment account must be selected"))
            }
        } else if (businessType == BusinessTransactionType.STOCK_ADJUSTMENT) {
            if (productId == null) {
                return Result.failure(Exception("Product must be selected for stock adjustment"))
            }
        } else if (businessType == BusinessTransactionType.PARTY_SETTLEMENT) {
            if ((partyId == null) || (toPartyId == null)) {
                return Result.failure(Exception("Both 'From Party' and 'To Party' must be selected for settlement"))
            }
            if (partyId == toPartyId) {
                return Result.failure(Exception("Source and destination parties must be different"))
            }
        } else if ((businessType == BusinessTransactionType.EQUITY_CONTRIBUTION) || (businessType == BusinessTransactionType.EQUITY_WITHDRAWAL)) {
            if (partyId == null) {
                return Result.failure(Exception("Partner must be selected"))
            }
            if (financialAccountId == null) {
                return Result.failure(Exception("Financial account must be selected"))
            }
        } else if (businessType == BusinessTransactionType.PROFIT_DISTRIBUTION) {
            if (partyId == null) {
                return Result.failure(Exception("Partner must be selected"))
            }
        } else {
            if (partyId == null) {
                return Result.failure(Exception("Party must be selected"))
            }
            if ((businessType == BusinessTransactionType.PAYMENT_MADE || businessType == BusinessTransactionType.PAYMENT_RECEIVED) && financialAccountId == null) {
                return Result.failure(Exception("Financial account must be selected for payments"))
            }
        }

        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("User not authenticated"))

        // Mapping Business Type to DEBIT/CREDIT/TRANSFER/EXPENSE/STOCK_ADJUSTMENT/PARTY_SETTLEMENT/EQUITY
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

        // Net Cost Logic
        val finalAmount = amount

        val netCost = if (freightType == FreightType.BORN_BY_US && (businessType == BusinessTransactionType.PURCHASE || businessType == BusinessTransactionType.SALE)) {
            finalAmount.add(freightAmount)
        } else {
            finalAmount
        }

        // 2. Reverse effects of OLD transaction
        oldTransaction?.let { old ->
            if (old.businessType == BusinessTransactionType.TRANSFER) {
                if (old.financialAccountId != null && old.toFinancialAccountId != null) {
                    financialAccountRepository.transferBalance(
                        old.toFinancialAccountId,
                        old.financialAccountId,
                        old.amount,
                    )
                }
            } else if (old.financialAccountId != null) {
                val oldBalanceChange = when (old.businessType) {
                    BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> old.amount
                    BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE,
                    BusinessTransactionType.EQUITY_WITHDRAWAL,
                    BusinessTransactionType.PROFIT_DISTRIBUTION ->
                        old.amount.negate()
                    else -> BigDecimal.ZERO
                }
                if (oldBalanceChange != BigDecimal.ZERO) {
                    financialAccountRepository.updateBalance(old.financialAccountId, oldBalanceChange.negate())
                }
            }
        }

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
            amount = finalAmount,
            freightAmount = freightAmount,
            freightType = freightType,
            netCost = netCost,
            type = type,
            businessType = businessType,
            note = note,
            createdBy = userId,
            timestamp = oldTransaction?.timestamp ?: System.currentTimeMillis()
        )

        // 3. Save / Update Transaction
        val finalTransactionId = if (transactionId != null) {
            transactionRepository.updateTransaction(transaction)
            transactionId
        } else {
            transactionRepository.addTransaction(transaction)
        }

        // 4. Apply NEW effects
        if (businessType == BusinessTransactionType.TRANSFER) {
            if (financialAccountId != null && toFinancialAccountId != null) {
                financialAccountRepository.transferBalance(financialAccountId, toFinancialAccountId, amount)
            }
        } else if (financialAccountId != null) {
            val balanceChange = when (businessType) {
                BusinessTransactionType.PAYMENT_RECEIVED, BusinessTransactionType.EQUITY_CONTRIBUTION -> amount
                BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE, 
                BusinessTransactionType.EQUITY_WITHDRAWAL, BusinessTransactionType.PROFIT_DISTRIBUTION -> amount.negate()
                else -> BigDecimal.ZERO
            }
            if (balanceChange != BigDecimal.ZERO) {
                financialAccountRepository.updateBalance(financialAccountId, balanceChange)
            }
        }

        return Result.success(finalTransactionId)
    }
}
