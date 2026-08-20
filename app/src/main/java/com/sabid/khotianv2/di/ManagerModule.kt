package com.sabid.khotianv2.di

import com.sabid.khotianv2.data.local.PermissionManagerImpl
import com.sabid.khotianv2.data.local.SessionManagerImpl
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagerModule {
    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @Binds
    @Singleton
    abstract fun bindPermissionManager(impl: PermissionManagerImpl): PermissionManager
}
