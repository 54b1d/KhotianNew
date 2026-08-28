package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.AuditLogDao
import com.sabid.khotianv2.data.local.dao.PartyDao
import com.sabid.khotianv2.data.local.dao.TransactionDao
import com.sabid.khotianv2.data.local.entity.AuditAction
import com.sabid.khotianv2.data.local.entity.AuditLogEntity
import com.sabid.khotianv2.data.local.entity.TransactionType
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.repository.TransactionRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val partyDao: PartyDao,
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) : TransactionRepository {
    private val transactionAdapter = moshi.adapter(Transaction::class.java)

    override fun getTransactionsByParty(partyId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByParty(partyId).map { list ->
            list.map { it.toDomain() }
        }

    override fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccount(accountId).map { list ->
            list.map { it.toDomain() }
        }

    override fun getPartyBalance(partyId: Long): Flow<BigDecimal> =
        combine(
            partyDao.getPartyByIdFlow(partyId),
            transactionDao.getTransactionsByParty(partyId)
        ) { party, entities ->
            val openingBalance = party?.openingBalance ?: BigDecimal.ZERO
            entities.map { it.toDomain() }.fold(openingBalance) { acc, transaction ->
                when (transaction.type) {
                    com.sabid.khotianv2.domain.model.TransactionType.DEBIT -> acc.add(transaction.amount)
                    com.sabid.khotianv2.domain.model.TransactionType.CREDIT -> acc.subtract(transaction.amount)
                    com.sabid.khotianv2.domain.model.TransactionType.PARTY_SETTLEMENT -> {
                        if (transaction.partyId == partyId) {
                            acc.subtract(transaction.amount)
                        } else if (transaction.toPartyId == partyId) {
                            acc.add(transaction.amount)
                        } else acc
                    }
                    com.sabid.khotianv2.domain.model.TransactionType.EQUITY -> {
                        when (transaction.businessType) {
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.EQUITY_WITHDRAWAL -> acc.add(transaction.amount)
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.EQUITY_CONTRIBUTION, 
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.PROFIT_DISTRIBUTION -> acc.subtract(transaction.amount)
                            else -> acc
                        }
                    }
                    else -> acc
                }
            }
        }

    override fun getUnifiedLedger(partyId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByParty(partyId).map { list ->
            list.map { it.toDomain() }
        }

    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }

    override fun getTransactionsByDate(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDate(startTime, endTime).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addTransaction(transaction: Transaction): Long {
        val id = transactionDao.insertTransaction(transaction.toEntity())
        val userId = sessionManager.currentUserId.value ?: "unknown"
        auditLogDao.insertLog(
            AuditLogEntity(
                tableName = "transactions",
                recordId = id,
                action = AuditAction.INSERT,
                oldValuesJson = null,
                newValuesJson = transactionAdapter.toJson(transaction),
                userId = userId
            )
        )
        return id
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val oldTransaction = transactionDao.getTransactionById(transaction.id)?.toDomain()
        transactionDao.updateTransaction(transaction.toEntity())
        val userId = sessionManager.currentUserId.value ?: "unknown"
        auditLogDao.insertLog(
            AuditLogEntity(
                tableName = "transactions",
                recordId = transaction.id,
                action = AuditAction.UPDATE,
                oldValuesJson = oldTransaction?.let { transactionAdapter.toJson(it) },
                newValuesJson = transactionAdapter.toJson(transaction),
                userId = userId
            )
        )
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
        val userId = sessionManager.currentUserId.value ?: "unknown"
        auditLogDao.insertLog(
            AuditLogEntity(
                tableName = "transactions",
                recordId = transaction.id,
                action = AuditAction.DELETE,
                oldValuesJson = transactionAdapter.toJson(transaction),
                newValuesJson = null,
                userId = userId
            )
        )
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun getChildTransactions(parentId: Long): List<Transaction> =
        transactionDao.getChildTransactions(parentId).map { it.toDomain() }

    override suspend fun deleteChildTransactions(parentId: Long) =
        transactionDao.deleteChildTransactions(parentId)
}
