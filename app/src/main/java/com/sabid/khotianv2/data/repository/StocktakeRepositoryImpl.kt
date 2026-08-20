package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.StocktakeDao
import com.sabid.khotianv2.domain.model.Stocktake
import com.sabid.khotianv2.domain.repository.StocktakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StocktakeRepositoryImpl @Inject constructor(
    private val stocktakeDao: StocktakeDao
) : StocktakeRepository {
    override fun getAllStocktakes(): Flow<List<Stocktake>> =
        stocktakeDao.getAllStocktakes().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addStocktake(stocktake: Stocktake): Long =
        stocktakeDao.insertStocktake(stocktake.toEntity())

    override suspend fun getLatestStocktakeForProduct(productId: Long): Stocktake? =
        stocktakeDao.getLatestStocktakeForProduct(productId)?.toDomain()

    override fun getLatestStocktakeForProductFlow(productId: Long): Flow<Stocktake?> =
        stocktakeDao.getLatestStocktakeForProductFlow(productId).map { it?.toDomain() }
}