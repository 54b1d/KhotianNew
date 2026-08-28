package com.sabid.khotianv2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sabid.khotianv2.data.local.converter.AppConverters
import com.sabid.khotianv2.data.local.dao.*
import com.sabid.khotianv2.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        RoleEntity::class,
        PermissionEntity::class,
        RolePermissionCrossRef::class,
        PartyEntity::class,
        ProductEntity::class,
        TransactionEntity::class,
        CrushingBatchEntity::class,
        AuditLogEntity::class,
        UnitEntity::class,
        FinancialAccountEntity::class,
        ExpenseCategoryEntity::class,
        StocktakeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun partyDao(): PartyDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun crushingBatchDao(): CrushingBatchDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun unitDao(): UnitDao
    abstract fun dataImportDao(): DataImportDao
    abstract fun financialAccountDao(): FinancialAccountDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun stocktakeDao(): StocktakeDao
}
