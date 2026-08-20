package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.ProfitLossReport
import com.sabid.khotianv2.domain.repository.ExpenseCategoryRepository
import com.sabid.khotianv2.domain.repository.ProductRepository
import com.sabid.khotianv2.domain.repository.StocktakeRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject

class GetProfitLossUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val productRepository: ProductRepository,
    private val stocktakeRepository: StocktakeRepository
) {
    operator fun invoke(): Flow<ProfitLossReport> {
        return combine(
            transactionRepository.getAllTransactions(),
            expenseCategoryRepository.getAllCategories(),
            productRepository.getAllProducts(),
            stocktakeRepository.getAllStocktakes()
        ) { transactions, categories, products, stocktakes ->
            val categoryMap = categories.associateBy({ it.id }, { it.name })
            
            var totalSales = BigDecimal.ZERO
            var totalPurchases = BigDecimal.ZERO
            var totalExpenses = BigDecimal.ZERO
            val expensesByCategory = mutableMapOf<String, BigDecimal>()

            transactions.forEach { tx ->
                when (tx.businessType) {
                    BusinessTransactionType.SALE -> {
                        totalSales = totalSales.add(tx.amount)
                    }
                    BusinessTransactionType.PURCHASE -> {
                        totalPurchases = totalPurchases.add(tx.amount)
                    }
                    BusinessTransactionType.EXPENSE -> {
                        totalExpenses = totalExpenses.add(tx.amount)
                        val categoryName = tx.expenseCategoryId?.let { categoryMap[it] } ?: "Uncategorized"
                        expensesByCategory[categoryName] = (expensesByCategory[categoryName] ?: BigDecimal.ZERO).add(tx.amount)
                    }
                    else -> {}
                }
            }

            // Calculate Closing Stock Value: latest Stocktake for each product
            var closingStockValue = BigDecimal.ZERO
            products.forEach { product ->
                val latestStocktake = stocktakes
                    .filter { it.productId == product.id }
                    .maxByOrNull { it.timestamp }
                if (latestStocktake != null) {
                    closingStockValue = closingStockValue.add(latestStocktake.physicalQuantity.multiply(latestStocktake.unitPrice))
                }
            }

            // Calculate Opening Stock Value: For now, assume it's 0 if no period specified, 
            // or we could use the openingBalance from products if we had a price.
            // As a simplified logic for "all time" report:
            val openingStockValue = BigDecimal.ZERO 

            // COGS = (Opening Stock + Purchases) - Closing Stock
            val costOfGoodsSold = openingStockValue.add(totalPurchases).subtract(closingStockValue)

            // Gross Profit = Total Sales - COGS
            val grossProfit = totalSales.subtract(costOfGoodsSold)

            // Net Profit = Gross Profit - Total Expenses
            val netProfit = grossProfit.subtract(totalExpenses)

            ProfitLossReport(
                totalSales = totalSales,
                totalPurchases = totalPurchases,
                totalExpenses = totalExpenses,
                openingStockValue = openingStockValue,
                closingStockValue = closingStockValue,
                costOfGoodsSold = costOfGoodsSold,
                grossProfit = grossProfit,
                expensesByCategory = expensesByCategory,
                netProfit = netProfit
            )
        }
    }
}
