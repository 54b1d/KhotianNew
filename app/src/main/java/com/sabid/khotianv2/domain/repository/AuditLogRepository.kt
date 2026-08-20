package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAllLogs(): Flow<List<AuditLog>>
    suspend fun addLog(log: AuditLog)
}
