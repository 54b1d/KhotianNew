package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.ProductDao
import com.sabid.khotianv2.data.local.dao.TransactionDao
import com.sabid.khotianv2.data.local.dao.UnitDao
import com.sabid.khotianv2.domain.model.Product
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.repository.ProductStock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao,
    private val unitDao: UnitDao
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

    override fun getProductStock(): Flow<List<ProductStock>> = combine(
        productDao.getAllProducts(),
        transactionDao.getAllTransactions(),
        unitDao.getAllUnits()
    ) { products, transactions, units ->
        products.map { product ->
            val productTransactions = transactions.filter { it.productId == product.id }
            var baseStock = BigDecimal.ZERO
            productTransactions.forEach { tx ->
                val qty = tx.baseQuantity ?: BigDecimal.ZERO
                // BusinessTransactionType is an enum in data layer too
                val type = tx.businessType
                if (type == com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PURCHASE) {
                    baseStock = baseStock.add(qty)
                } else if (type == com.sabid.khotianv2.data.local.entity.BusinessTransactionType.SALE) {
                    baseStock = baseStock.subtract(qty)
                } else if (type == com.sabid.khotianv2.data.local.entity.BusinessTransactionType.STOCK_ADJUSTMENT) {
                    baseStock = baseStock.add(qty)
                }
            }
            val defaultUnit = units.find { it.id == product.defaultUnitId }
            val stockInDefaultUnit = if (defaultUnit != null && defaultUnit.multiplier != BigDecimal.ZERO) {
                baseStock.divide(defaultUnit.multiplier, 2, RoundingMode.HALF_UP)
            } else {
                null
            }
            ProductStock(
                productId = product.id,
                productName = product.name,
                baseStock = baseStock,
                defaultUnitSymbol = defaultUnit?.symbol,
                stockInDefaultUnit = stockInDefaultUnit
            )
        }
    }
}
