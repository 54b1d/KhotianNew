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
        partyId: Long? = null,
        productId: Long? = null,
        unitId: Long? = null,
        financialAccountId: Long? = null,
        toFinancialAccountId: Long? = null,
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

        if (businessType == BusinessTransactionType.TRANSFER) {
            if (financialAccountId == null || toFinancialAccountId == null) {
                return Result.failure(Exception("Both source and destination accounts must be selected for transfer"))
            }
            if (financialAccountId == toFinancialAccountId) {
                return Result.failure(Exception("Source and destination accounts must be different"))
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

        // Mapping Business Type to DEBIT/CREDIT/TRANSFER
        val type = when (businessType) {
            BusinessTransactionType.SALE, BusinessTransactionType.PAYMENT_MADE -> TransactionType.DEBIT
            BusinessTransactionType.PURCHASE, BusinessTransactionType.PAYMENT_RECEIVED -> TransactionType.CREDIT
            BusinessTransactionType.TRANSFER -> TransactionType.TRANSFER
        }

        // Net Cost Logic
        val netCost = if (freightType == FreightType.BORN_BY_US) {
            amount.add(freightAmount)
        } else {
            amount
        }

        val transaction = Transaction(
            partyId = partyId,
            productId = productId,
            unitId = unitId,
            financialAccountId = financialAccountId,
            toFinancialAccountId = toFinancialAccountId,
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
            createdBy = userId
        )

        val transactionId = transactionRepository.addTransaction(transaction)

        // Update Financial Account Balance
        if (businessType == BusinessTransactionType.TRANSFER) {
            if (financialAccountId != null && toFinancialAccountId != null) {
                financialAccountRepository.transferBalance(financialAccountId, toFinancialAccountId, amount)
            }
        } else if (financialAccountId != null) {
            val balanceChange = when (businessType) {
                BusinessTransactionType.PAYMENT_RECEIVED -> amount
                BusinessTransactionType.PAYMENT_MADE -> amount.negate()
                else -> BigDecimal.ZERO
            }
            if (balanceChange != BigDecimal.ZERO) {
                financialAccountRepository.updateBalance(financialAccountId, balanceChange)
            }
        }

        return Result.success(transactionId)
    }
}
