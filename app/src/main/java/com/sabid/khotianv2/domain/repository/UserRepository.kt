package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(username: String, pin: String): Result<User>
    suspend fun createUser(user: User, passwordHash: String): Result<Long>
    fun getCurrentUser(): Flow<User?>
    suspend fun hasUsers(): Boolean
    suspend fun initializeSystem(adminUsername: String, adminPin: String): Result<Unit>
}
