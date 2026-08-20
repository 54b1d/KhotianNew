package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.Stocktake
import kotlinx.coroutines.flow.Flow

interface StocktakeRepository {
    fun getAllStocktakes(): Flow<List<Stocktake>>
    suspend fun addStocktake(stocktake: Stocktake): Long
    suspend fun getLatestStocktakeForProduct(productId: Long): Stocktake?
    fun getLatestStocktakeForProductFlow(productId: Long): Flow<Stocktake?>
}