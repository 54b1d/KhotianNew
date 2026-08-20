package com.sabid.khotianv2.di

import com.sabid.khotianv2.data.repository.*
import com.sabid.khotianv2.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindKhotianRepository(
        khotianRepositoryImpl: KhotianRepositoryImpl
    ): KhotianRepository
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindBusinessRepository(
        businessRepositoryImpl: BusinessRepositoryImpl
    ): BusinessRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCrushingRepository(
        crushingRepositoryImpl: CrushingRepositoryImpl
    ): CrushingRepository

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(
        auditLogRepositoryImpl: AuditLogRepositoryImpl
    ): AuditLogRepository
}