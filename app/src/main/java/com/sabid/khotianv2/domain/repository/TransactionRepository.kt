package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface TransactionRepository {
    fun getTransactionsByParty(partyId: Long): Flow<List<Transaction>>
    fun getPartyBalance(partyId: Long): Flow<BigDecimal>
    fun getUnifiedLedger(partyId: Long): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: Long): Transaction?
}
