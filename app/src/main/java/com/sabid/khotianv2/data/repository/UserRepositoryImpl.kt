package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.HashUtils
import com.sabid.khotianv2.data.local.dao.UserDao
import com.sabid.khotianv2.data.local.entity.PermissionEntity
import com.sabid.khotianv2.data.local.entity.RoleEntity
import com.sabid.khotianv2.data.local.entity.RoleWithPermissions
import com.sabid.khotianv2.data.local.entity.UserEntity
import com.sabid.khotianv2.data.local.entity.UserWithRole
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.model.Role
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

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsersWithRoles().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUser(userId.toLong())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllRoles(): Flow<List<Role>> {
        return userDao.getRolesWithPermissions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun upsertRole(role: Role): Result<Long> {
        return try {
            db.withTransaction {
                val roleId = userDao.insertRole(RoleEntity(id = role.id, name = role.name))
                userDao.updateRolePermissions(roleId, role.permissions)
                roleId
            }.let { Result.success(it) }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRole(roleId: Long): Result<Unit> {
        return try {
            userDao.deleteRole(roleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun UserEntity.toDomain() = User(
        id = id.toString(),
        username = username,
        roleId = roleId,
        roleName = null, // Handled in UserWithRole.toDomain()
        isActive = isActive
    )

    private fun UserWithRole.toDomain() = User(
        id = user.id.toString(),
        username = user.username,
        roleId = user.roleId,
        roleName = role?.name,
        isActive = user.isActive
    )

    private fun RoleWithPermissions.toDomain() = Role(
        id = role.id,
        name = role.name,
        permissions = permissions.map { it.flag }
    )
}
