package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.AuditLogDao
import com.sabid.khotianv2.data.local.dao.CrushingBatchDao
import com.sabid.khotianv2.data.local.entity.AuditAction
import com.sabid.khotianv2.data.local.entity.AuditLogEntity
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.CrushingBatch
import com.sabid.khotianv2.domain.repository.CrushingRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrushingRepositoryImpl @Inject constructor(
    private val crushingBatchDao: CrushingBatchDao,
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) : CrushingRepository {
    private val batchAdapter = moshi.adapter(CrushingBatch::class.java)

    override fun getAllBatches(): Flow<List<CrushingBatch>> =
        crushingBatchDao.getAllBatches().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addBatch(batch: CrushingBatch): Long {
        val id = crushingBatchDao.insertBatch(batch.toEntity())
        val userId = sessionManager.currentUserId.value ?: "unknown"
        auditLogDao.insertLog(
            AuditLogEntity(
                tableName = "crushing_batches",
                recordId = id,
                action = AuditAction.INSERT,
                oldValuesJson = null,
                newValuesJson = batchAdapter.toJson(batch),
                userId = userId
            )
        )
        return id
    }

    override suspend fun getBatchById(id: Long): CrushingBatch? {
        return crushingBatchDao.getBatchById(id)?.toDomain()
    }
}
