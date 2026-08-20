package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.AppUnit
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getAllUnits(): Flow<List<AppUnit>>
    suspend fun addUnit(unit: AppUnit): Long
    suspend fun updateUnit(unit: AppUnit)
    suspend fun deleteUnit(unit: AppUnit)
    suspend fun getUnitById(id: Long): AppUnit?
}
