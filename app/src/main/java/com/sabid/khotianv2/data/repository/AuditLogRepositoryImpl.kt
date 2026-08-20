package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.AuditLogDao
import com.sabid.khotianv2.domain.model.AuditLog
import com.sabid.khotianv2.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditLogRepository {
    override fun getAllLogs(): Flow<List<AuditLog>> =
        auditLogDao.getAllLogs().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addLog(log: AuditLog) {
        auditLogDao.insertLog(log.toEntity())
    }
}
