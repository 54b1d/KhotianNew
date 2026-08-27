package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.Role
import com.sabid.khotianv2.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(username: String, pin: String): Result<User>
    suspend fun createUser(user: User, passwordHash: String): Result<Long>
    fun getCurrentUser(): Flow<User?>
    suspend fun hasUsers(): Boolean
    suspend fun initializeSystem(adminUsername: String, adminPin: String): Result<Unit>
    
    fun getAllUsers(): Flow<List<User>>
    suspend fun deleteUser(userId: String): Result<Unit>
    
    fun getAllRoles(): Flow<List<Role>>
    suspend fun upsertRole(role: Role): Result<Long>
    suspend fun deleteRole(roleId: Long): Result<Unit>
}
