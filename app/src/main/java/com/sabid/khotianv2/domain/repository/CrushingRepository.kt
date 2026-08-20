package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.CrushingBatch
import kotlinx.coroutines.flow.Flow

interface CrushingRepository {
    fun getAllBatches(): Flow<List<CrushingBatch>>
    suspend fun addBatch(batch: CrushingBatch): Long
    suspend fun getBatchById(id: Long): CrushingBatch?
}
