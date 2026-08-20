package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.Product
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun getProductById(id: Long): Product?
    fun getProductStock(): Flow<List<ProductStock>>
}

data class ProductStock(
    val productId: Long,
    val productName: String,
    val baseStock: BigDecimal,
    val defaultUnitSymbol: String?,
    val stockInDefaultUnit: BigDecimal?
)
