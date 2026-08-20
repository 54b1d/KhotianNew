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
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        transactionId: Long? = null,
        partyId: Long? = null,
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
        note: String?
    ): Result<Long> {
        if (!permissionManager.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
            return Result.failure(Exception("Permission denied"))
        }

        // 1. Fetch OLD transaction if editing
        val oldTransaction = if (transactionId != null) {
            transactionRepository.getTransactionById(transactionId)
        } else null

        // Validation
        if (businessType == BusinessTransactionType.TRANSFER) {
            if (financialAccountId == null || toFinancialAccountId == null) {
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
        } else {
            if (partyId == null) {
                return Result.failure(Exception("Party must be selected"))
            }
            if ((businessType == BusinessTransactionType.PAYMENT_MADE || businessType == BusinessTransactionType.PAYMENT_RECEIVED) && financialAccountId == null) {
                return Result.failure(Exception("Financial account must be selected for payments"))
            }
        }

        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("User not authenticated"))

        // Mapping Business Type to DEBIT/CREDIT/TRANSFER/EXPENSE/STOCK_ADJUSTMENT
        val type = when (businessType) {
            BusinessTransactionType.SALE, BusinessTransactionType.PAYMENT_MADE -> TransactionType.DEBIT
            BusinessTransactionType.PURCHASE, BusinessTransactionType.PAYMENT_RECEIVED -> TransactionType.CREDIT
            BusinessTransactionType.TRANSFER -> TransactionType.TRANSFER
            BusinessTransactionType.EXPENSE -> TransactionType.EXPENSE
            BusinessTransactionType.STOCK_ADJUSTMENT -> TransactionType.STOCK_ADJUSTMENT
        }

        // Net Cost Logic
        val netCost = if (freightType == FreightType.BORN_BY_US) {
            amount.add(freightAmount)
        } else {
            amount
        }

        // 2. Reverse effects of OLD transaction
        if (oldTransaction != null) {
            if (oldTransaction.businessType == BusinessTransactionType.TRANSFER) {
                if (oldTransaction.financialAccountId != null && oldTransaction.toFinancialAccountId != null) {
                    financialAccountRepository.transferBalance(
                        oldTransaction.toFinancialAccountId,
                        oldTransaction.financialAccountId,
                        oldTransaction.amount
                    )
                }
            } else if (oldTransaction.financialAccountId != null) {
                val oldBalanceChange = when (oldTransaction.businessType) {
                    BusinessTransactionType.PAYMENT_RECEIVED -> oldTransaction.amount
                    BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE -> oldTransaction.amount.negate()
                    else -> BigDecimal.ZERO
                }
                if (oldBalanceChange != BigDecimal.ZERO) {
                    financialAccountRepository.updateBalance(oldTransaction.financialAccountId, oldBalanceChange.negate())
                }
            }
            // Product Stock is calculated on-the-fly, so no manual reversal needed here if using the transactions table.
            // If it was stored, we would reverse it here.
        }

        val transaction = Transaction(
            id = transactionId ?: 0,
            partyId = partyId,
            productId = productId,
            unitId = unitId,
            financialAccountId = financialAccountId,
            toFinancialAccountId = toFinancialAccountId,
            expenseCategoryId = expenseCategoryId,
            quantity = quantity,
            baseQuantity = baseQuantity,
            rate = rate,
            amount = amount,
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
                BusinessTransactionType.PAYMENT_RECEIVED -> amount
                BusinessTransactionType.PAYMENT_MADE, BusinessTransactionType.EXPENSE -> amount.negate()
                else -> BigDecimal.ZERO
            }
            if (balanceChange != BigDecimal.ZERO) {
                financialAccountRepository.updateBalance(financialAccountId, balanceChange)
            }
        }

        return Result.success(finalTransactionId)
    }
}
