package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.UnitDao
import com.sabid.khotianv2.domain.model.AppUnit
import com.sabid.khotianv2.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UnitRepositoryImpl @Inject constructor(
    private val unitDao: UnitDao
) : UnitRepository {
    override fun getAllUnits(): Flow<List<AppUnit>> =
        unitDao.getAllUnits().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addUnit(unit: AppUnit): Long =
        unitDao.insertUnit(unit.toEntity())

    override suspend fun updateUnit(unit: AppUnit) =
        unitDao.updateUnit(unit.toEntity())

    override suspend fun deleteUnit(unit: AppUnit) =
        unitDao.deleteUnit(unit.toEntity())

    override suspend fun getUnitById(id: Long): AppUnit? =
        unitDao.getUnitById(id)?.toDomain()
}
