package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.TransactionRepository
import java.math.BigDecimal
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val permissionManager: PermissionManager,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        partyId: Long,
        productId: Long? = null,
        quantity: BigDecimal? = null,
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

        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("User not authenticated"))

        // Mapping Business Type to DEBIT/CREDIT
        val type = when (businessType) {
            BusinessTransactionType.SALE, BusinessTransactionType.PAYMENT_MADE -> TransactionType.DEBIT
            BusinessTransactionType.PURCHASE, BusinessTransactionType.PAYMENT_RECEIVED -> TransactionType.CREDIT
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
            quantity = quantity,
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

        return Result.success(transactionId)
    }
}
