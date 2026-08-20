package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.*
import com.sabid.khotianv2.domain.model.BackupData
import com.sabid.khotianv2.domain.repository.BackupRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val unitDao: UnitDao,
    private val productDao: ProductDao,
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao,
    private val financialAccountDao: FinancialAccountDao,
    private val expenseCategoryDao: ExpenseCategoryDao,
    private val stocktakeDao: StocktakeDao,
    private val dataImportDao: DataImportDao,
    private val moshi: Moshi
) : BackupRepository {

    private val backupAdapter = moshi.adapter(BackupData::class.java)

    override suspend fun exportData(): String {
        val backupData = BackupData(
            units = unitDao.getAllUnits().first().map { it.toDomain() },
            products = productDao.getAllProducts().first().map { it.toDomain() },
            parties = partyDao.getAllParties().first().map { it.toDomain() },
            transactions = transactionDao.getAllTransactions().first().map { it.toDomain() },
            financialAccounts = financialAccountDao.getAllAccounts().first().map { it.toDomain() },
            expenseCategories = expenseCategoryDao.getAllCategories().first().map { it.toDomain() },
            stocktakes = stocktakeDao.getAllStocktakes().first().map { it.toDomain() }
        )
        return backupAdapter.toJson(backupData)
    }

    override suspend fun importData(jsonData: String) {
        val backupData = backupAdapter.fromJson(jsonData) ?: throw IllegalArgumentException("Invalid backup data")
        
        dataImportDao.importAll(
            units = backupData.units.map { it.toEntity() },
            products = backupData.products.map { it.toEntity() },
            parties = backupData.parties.map { it.toEntity() },
            transactions = backupData.transactions.map { it.toEntity() },
            financialAccounts = backupData.financialAccounts.map { it.toEntity() },
            expenseCategories = backupData.expenseCategories.map { it.toEntity() },
            stocktakes = backupData.stocktakes.map { it.toEntity() }
        )
    }
}
