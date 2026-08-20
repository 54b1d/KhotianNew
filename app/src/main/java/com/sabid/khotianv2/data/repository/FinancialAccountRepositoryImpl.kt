package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.FinancialAccountDao
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class FinancialAccountRepositoryImpl @Inject constructor(
    private val accountDao: FinancialAccountDao
) : FinancialAccountRepository {
    override fun getAllAccounts(): Flow<List<FinancialAccount>> =
        accountDao.getAllAccounts().map { entities -> entities.map { it.toDomain() } }

    override fun getAccountById(id: Long): Flow<FinancialAccount?> =
        accountDao.getAccountByIdFlow(id).map { it?.toDomain() }

    override suspend fun getAccountByIdSync(id: Long): FinancialAccount? =
        accountDao.getAccountById(id)?.toDomain()

    override suspend fun addAccount(account: FinancialAccount): Long =
        accountDao.insertAccount(account.toEntity())

    override suspend fun updateAccount(account: FinancialAccount) =
        accountDao.updateAccount(account.toEntity())

    override suspend fun deleteAccount(account: FinancialAccount) =
        accountDao.deleteAccount(account.toEntity())

    override suspend fun updateBalance(accountId: Long, amount: BigDecimal) =
        accountDao.updateBalance(accountId, amount)

    override suspend fun transferBalance(fromAccountId: Long, toAccountId: Long, amount: BigDecimal) =
        accountDao.transferBalance(fromAccountId, toAccountId, amount)
}
