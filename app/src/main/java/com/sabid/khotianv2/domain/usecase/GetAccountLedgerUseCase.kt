package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class GetAccountLedgerUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val financialAccountRepository: FinancialAccountRepository
) {
    operator fun invoke(accountId: Long): Flow<Pair<BigDecimal, List<Transaction>>> {
        return combine(
            financialAccountRepository.getAccountById(accountId).map { it?.openingBalance ?: BigDecimal.ZERO },
            transactionRepository.getTransactionsByAccount(accountId)
        ) { openingBalance, transactions ->
            openingBalance to transactions
        }
    }
}
