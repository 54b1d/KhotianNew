package com.sabid.khotianv2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableName: String,
    val recordId: Long,
    val action: AuditAction,
    val oldValuesJson: String?,
    val newValuesJson: String?,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AuditAction {
    INSERT, UPDATE, DELETE
}
