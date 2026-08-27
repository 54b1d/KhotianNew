package com.sabid.khotianv2.data.local.dao

import androidx.room.*
import com.sabid.khotianv2.data.local.entity.RoleEntity
import com.sabid.khotianv2.data.local.entity.UserEntity
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.data.local.entity.RolePermissionCrossRef
import com.sabid.khotianv2.data.local.entity.RoleWithPermissions
import com.sabid.khotianv2.data.local.entity.UserWithRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Transaction
    @Query("SELECT * FROM users")
    fun getAllUsersWithRoles(): Flow<List<UserWithRole>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    @Query("DELETE FROM roles WHERE id = :roleId")
    suspend fun deleteRole(roleId: Long)

    @Transaction
    @Query("SELECT * FROM roles")
    fun getRolesWithPermissions(): Flow<List<RoleWithPermissions>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun getRoleCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPermissions(permissions: List<com.sabid.khotianv2.data.local.entity.PermissionEntity>)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("""
        SELECT flag FROM role_permission_cross_ref 
        WHERE roleId = (SELECT roleId FROM users WHERE id = :userId)
    """)
    fun getUserPermissions(userId: Long): Flow<List<PermissionType>>

    @Query("SELECT * FROM roles")
    fun getAllRoles(): Flow<List<RoleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity): Long

    @Query("DELETE FROM role_permission_cross_ref WHERE roleId = :roleId")
    suspend fun clearRolePermissions(roleId: Long)

    @Insert
    suspend fun insertRolePermissions(crossRefs: List<RolePermissionCrossRef>)
    
    @Transaction
    suspend fun updateRolePermissions(roleId: Long, permissions: List<PermissionType>) {
        clearRolePermissions(roleId)
        val refs = permissions.map { RolePermissionCrossRef(roleId, it) }
        insertRolePermissions(refs)
    }
}
