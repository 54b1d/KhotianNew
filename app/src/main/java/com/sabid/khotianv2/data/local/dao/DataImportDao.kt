package com.sabid.khotianv2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.sabid.khotianv2.data.local.entity.FinancialAccountEntity
import com.sabid.khotianv2.data.local.entity.PartyEntity
import com.sabid.khotianv2.data.local.entity.ProductEntity
import com.sabid.khotianv2.data.local.entity.TransactionEntity
import com.sabid.khotianv2.data.local.entity.UnitEntity
import com.sabid.khotianv2.data.local.entity.ExpenseCategoryEntity

@Dao
interface DataImportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParties(parties: List<PartyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialAccounts(accounts: List<FinancialAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategories(categories: List<ExpenseCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocktakes(stocktakes: List<com.sabid.khotianv2.data.local.entity.StocktakeEntity>)

    @Transaction
    suspend fun importAll(
        units: List<UnitEntity>,
        products: List<ProductEntity>,
        parties: List<PartyEntity>,
        transactions: List<TransactionEntity>,
        financialAccounts: List<FinancialAccountEntity>,
        expenseCategories: List<ExpenseCategoryEntity>,
        stocktakes: List<com.sabid.khotianv2.data.local.entity.StocktakeEntity> = emptyList()
    ) {
        insertUnits(units)
        insertProducts(products)
        insertParties(parties)
        insertFinancialAccounts(financialAccounts)
        insertExpenseCategories(expenseCategories)
        insertStocktakes(stocktakes)
        insertTransactions(transactions)
    }
}
