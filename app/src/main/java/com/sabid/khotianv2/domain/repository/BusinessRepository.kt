package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.CrushingBatch
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface BusinessRepository {
    fun getParties(): Flow<List<Party>>
    fun getParty(id: Long): Flow<Party?>
    fun getTransactions(partyId: Long): Flow<List<Transaction>>
    fun getPartyBalance(partyId: Long): Flow<BigDecimal>
    suspend fun addTransaction(transaction: Transaction): Result<Long>
    suspend fun addParty(party: Party): Result<Long>
    
    fun getCrushingBatches(): Flow<List<CrushingBatch>>
    suspend fun addCrushingBatch(batch: CrushingBatch): Result<Long>
}
