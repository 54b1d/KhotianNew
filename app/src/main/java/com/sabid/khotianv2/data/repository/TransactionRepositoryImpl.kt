package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.AuditLogDao
import com.sabid.khotianv2.data.local.dao.TransactionDao
import com.sabid.khotianv2.data.local.entity.AuditAction
import com.sabid.khotianv2.data.local.entity.AuditLogEntity
import com.sabid.khotianv2.data.local.entity.TransactionType
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.repository.TransactionRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) : TransactionRepository {
    private val transactionAdapter = moshi.adapter(Transaction::class.java)

    override fun getTransactionsByParty(partyId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByParty(partyId).map { list ->
            list.map { it.toDomain() }
        }

    override fun getPartyBalance(partyId: Long): Flow<BigDecimal> =
        transactionDao.getTransactionsByParty(partyId).map { list ->
            list.fold(BigDecimal.ZERO) { acc, entity ->
                if (entity.type == TransactionType.DEBIT) acc.add(entity.netCost) else acc.subtract(entity.netCost)
            }
        }

    override fun getUnifiedLedger(partyId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByParty(partyId).map { list ->
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
}
