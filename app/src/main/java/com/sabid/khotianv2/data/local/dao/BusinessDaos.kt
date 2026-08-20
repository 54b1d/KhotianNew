package com.sabid.khotianv2.data.local.dao

import androidx.room.*
import com.sabid.khotianv2.data.local.entity.AuditLogEntity
import com.sabid.khotianv2.data.local.entity.CrushingBatchEntity
import com.sabid.khotianv2.data.local.entity.PartyEntity
import com.sabid.khotianv2.data.local.entity.ProductEntity
import com.sabid.khotianv2.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: PartyEntity): Long

    @Query("SELECT * FROM parties")
    fun getAllParties(): Flow<List<PartyEntity>>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): PartyEntity?
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
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE partyId = :partyId ORDER BY timestamp DESC")
    fun getTransactionsByParty(partyId: Long): Flow<List<TransactionEntity>>

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
