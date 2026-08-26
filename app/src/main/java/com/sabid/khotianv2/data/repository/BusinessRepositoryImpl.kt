package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.*
import com.sabid.khotianv2.data.local.entity.*
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.CrushingBatch
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.repository.BusinessRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class BusinessRepositoryImpl @Inject constructor(
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao,
    private val crushingBatchDao: CrushingBatchDao,
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) : BusinessRepository {
    private val transactionAdapter = moshi.adapter(Transaction::class.java)
    private val batchAdapter = moshi.adapter(CrushingBatch::class.java)
    private val partyAdapter = moshi.adapter(Party::class.java)

    override fun getParties(): Flow<List<Party>> = partyDao.getAllParties().map { list ->
        list.map { it.toDomain() }
    }

    override fun getParty(id: Long): Flow<Party?> = partyDao.getPartyByIdFlow(id).map { 
        it?.toDomain()
    }

    override fun getTransactions(partyId: Long): Flow<List<Transaction>> = 
        transactionDao.getTransactionsByParty(partyId).map { list ->
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
                    com.sabid.khotianv2.domain.model.TransactionType.DEBIT -> acc.add(transaction.netCost)
                    com.sabid.khotianv2.domain.model.TransactionType.CREDIT -> acc.subtract(transaction.netCost)
                    com.sabid.khotianv2.domain.model.TransactionType.PARTY_SETTLEMENT -> {
                        if (transaction.partyId == partyId) acc.subtract(transaction.netCost)
                        else if (transaction.toPartyId == partyId) acc.add(transaction.netCost)
                        else acc
                    }
                    com.sabid.khotianv2.domain.model.TransactionType.EQUITY -> {
                        when (transaction.businessType) {
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.EQUITY_WITHDRAWAL -> acc.add(transaction.netCost)
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.EQUITY_CONTRIBUTION, 
                            com.sabid.khotianv2.domain.model.BusinessTransactionType.PROFIT_DISTRIBUTION -> acc.subtract(transaction.netCost)
                            else -> acc
                        }
                    }
                    else -> acc
                }
            }
        }

    override suspend fun addTransaction(transaction: Transaction): Result<Long> {
        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("Not logged in"))
        val entity = transaction.toEntity().copy(createdBy = userId)
        val id = transactionDao.insertTransaction(entity)
        
        // Audit Log
        auditLogDao.insertLog(AuditLogEntity(
            tableName = "transactions",
            recordId = id,
            action = AuditAction.INSERT,
            oldValuesJson = null,
            newValuesJson = transactionAdapter.toJson(transaction),
            userId = userId
        ))
        
        return Result.success(id)
    }

    override suspend fun addParty(party: Party): Result<Long> {
        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("Not logged in"))
        val entity = party.toEntity()
        val id = partyDao.insertParty(entity)
        
        auditLogDao.insertLog(AuditLogEntity(
            tableName = "parties",
            recordId = id,
            action = AuditAction.INSERT,
            oldValuesJson = null,
            newValuesJson = partyAdapter.toJson(party),
            userId = userId
        ))
        
        return Result.success(id)
    }

    override fun getCrushingBatches(): Flow<List<CrushingBatch>> = 
        crushingBatchDao.getAllBatches().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addCrushingBatch(batch: CrushingBatch): Result<Long> {
        val userId = sessionManager.currentUserId.value ?: return Result.failure(Exception("Not logged in"))
        val entity = batch.toEntity()
        val id = crushingBatchDao.insertBatch(entity)
        
        auditLogDao.insertLog(AuditLogEntity(
            tableName = "crushing_batches",
            recordId = id,
            action = AuditAction.INSERT,
            oldValuesJson = null,
            newValuesJson = batchAdapter.toJson(batch),
            userId = userId
        ))
        
        return Result.success(id)
    }
}
