package com.sabid.khotianv2.data.local.dao

import androidx.room.*
import com.sabid.khotianv2.data.local.entity.AuditLogEntity
import com.sabid.khotianv2.data.local.entity.CrushingBatchEntity
import com.sabid.khotianv2.data.local.entity.PartyEntity
import com.sabid.khotianv2.data.local.entity.ProductEntity
import com.sabid.khotianv2.data.local.entity.TransactionEntity
import com.sabid.khotianv2.data.local.entity.FinancialAccountEntity
import com.sabid.khotianv2.data.local.entity.UnitEntity
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity): Long

    @Query("SELECT * FROM units")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getUnitById(id: Long): UnitEntity?

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Delete
    suspend fun deleteUnit(unit: UnitEntity)
}

@Dao
interface PartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Query("SELECT * FROM parties")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): PartyEntity?

    @Query("SELECT * FROM parties WHERE id = :id")
    fun getPartyByIdFlow(id: Long): Flow<PartyEntity?>
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface FinancialAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccountEntity): Long

    @Query("SELECT * FROM financial_accounts")
    fun getAllAccounts(): Flow<List<FinancialAccountEntity>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): FinancialAccountEntity?

    @Query("SELECT * FROM financial_accounts WHERE id = :id")
    fun getAccountByIdFlow(id: Long): Flow<FinancialAccountEntity?>

    @Update
    suspend fun updateAccount(account: FinancialAccountEntity)

    @Delete
    suspend fun deleteAccount(account: FinancialAccountEntity)

    @Query("UPDATE financial_accounts SET currentBalance = currentBalance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: BigDecimal)

    @Transaction
    suspend fun transferBalance(fromAccountId: Long, toAccountId: Long, amount: BigDecimal) {
        updateBalance(fromAccountId, amount.negate())
        updateBalance(toAccountId, amount)
    }
}

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE partyId = :partyId ORDER BY timestamp DESC")
    fun getTransactionsByParty(partyId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE financialAccountId = :accountId OR toFinancialAccountId = :accountId ORDER BY timestamp DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}

@Dao
interface CrushingBatchDao {
    @Insert
    suspend fun insertBatch(batch: CrushingBatchEntity): Long

    @Query("SELECT * FROM crushing_batches ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<CrushingBatchEntity>>

    @Query("SELECT * FROM crushing_batches WHERE id = :id")
    suspend fun getBatchById(id: Long): CrushingBatchEntity?
}

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>
}
