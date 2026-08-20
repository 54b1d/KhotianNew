package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.ProductDao
import com.sabid.khotianv2.domain.model.Product
import com.sabid.khotianv2.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addProduct(product: Product): Long =
        productDao.insertProduct(product.toEntity())

    override suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product.toEntity())

    override suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product.toEntity())

    override suspend fun getProductById(id: Long): Product? =
        productDao.getProductById(id)?.toDomain()
}
