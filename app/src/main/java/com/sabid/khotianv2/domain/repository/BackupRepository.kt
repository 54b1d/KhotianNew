package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.BackupData

interface BackupRepository {
    suspend fun exportData(): String
    suspend fun importData(jsonData: String)
}
