package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.HashUtils
import com.sabid.khotianv2.data.local.dao.UserDao
import com.sabid.khotianv2.data.local.entity.PermissionEntity
import com.sabid.khotianv2.data.local.entity.RoleEntity
import com.sabid.khotianv2.data.local.entity.UserEntity
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.model.User
import com.sabid.khotianv2.domain.repository.UserRepository
import androidx.room.withTransaction
import com.sabid.khotianv2.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : UserRepository {
    override suspend fun login(username: String, pin: String): Result<User> {
        val userEntity = userDao.getUserByUsername(username)
        return if (userEntity != null && userEntity.passwordHash == HashUtils.sha256(pin)) {
            sessionManager.startSession(userEntity.id.toString())
            Result.success(userEntity.toDomain())
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun createUser(user: User, passwordHash: String): Result<Long> {
        val entity = UserEntity(
            username = user.username,
            passwordHash = HashUtils.sha256(passwordHash),
            roleId = user.roleId,
            isActive = user.isActive
        )
        val id = userDao.insertUser(entity)
        return Result.success(id)
    }

    override fun getCurrentUser(): Flow<User?> {
        return sessionManager.currentUserId.map { id ->
            id?.toLongOrNull()?.let { userDao.getUserById(it)?.toDomain() }
        }
    }

    override suspend fun hasUsers(): Boolean {
        return userDao.getUserCount() > 0
    }

    override suspend fun initializeSystem(adminUsername: String, adminPin: String): Result<Unit> {
        return try {
            db.withTransaction<Unit> {
                // 1. Insert all permission types
                val permissions = PermissionType.values().map { PermissionEntity(it) }
                userDao.insertPermissions(permissions)

                // 2. Create Admin Role
                val adminRoleId = userDao.insertRole(RoleEntity(name = "Administrator"))

                // 3. Assign all permissions to Admin Role
                userDao.updateRolePermissions(adminRoleId, PermissionType.values().toList())

                // 4. Create Admin User
                val adminUser = UserEntity(
                    username = adminUsername,
                    passwordHash = HashUtils.sha256(adminPin),
                    roleId = adminRoleId
                )
                val adminId = userDao.insertUser(adminUser)
                sessionManager.startSession(adminId.toString())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserEntity.toDomain() = User(
        id = id.toString(),
        username = username,
        roleId = roleId,
        roleName = null, // In a real app, join with roles table
        isActive = isActive
    )
}
