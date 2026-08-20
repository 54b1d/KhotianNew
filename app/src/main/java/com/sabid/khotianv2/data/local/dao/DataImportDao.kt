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

    @Transaction
    suspend fun importAll(
        units: List<UnitEntity>,
        products: List<ProductEntity>,
        parties: List<PartyEntity>,
        transactions: List<TransactionEntity>,
        financialAccounts: List<FinancialAccountEntity>
    ) {
        insertUnits(units)
        insertProducts(products)
        insertParties(parties)
        insertFinancialAccounts(financialAccounts)
        insertTransactions(transactions)
    }
}
