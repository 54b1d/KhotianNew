package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.FinancialAccount
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface FinancialAccountRepository {
    fun getAllAccounts(): Flow<List<FinancialAccount>>
    fun getAccountById(id: Long): Flow<FinancialAccount?>
    suspend fun getAccountByIdSync(id: Long): FinancialAccount?
    suspend fun addAccount(account: FinancialAccount): Long
    suspend fun updateAccount(account: FinancialAccount)
    suspend fun deleteAccount(account: FinancialAccount)
    suspend fun updateBalance(accountId: Long, amount: BigDecimal)
    suspend fun transferBalance(fromAccountId: Long, toAccountId: Long, amount: BigDecimal)
}
