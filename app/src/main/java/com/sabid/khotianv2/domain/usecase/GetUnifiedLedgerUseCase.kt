package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnifiedLedgerUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(partyId: Long): Flow<List<Transaction>> {
        return transactionRepository.getUnifiedLedger(partyId)
    }
}
