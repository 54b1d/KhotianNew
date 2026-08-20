package com.sabid.khotianv2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sabid.khotianv2.domain.model.PermissionType

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val roleId: Long?,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "permissions")
data class PermissionEntity(
    @PrimaryKey val flag: PermissionType
)

@Entity(
    tableName = "role_permission_cross_ref",
    primaryKeys = ["roleId", "flag"],
    foreignKeys = [
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PermissionEntity::class,
            parentColumns = ["flag"],
            childColumns = ["flag"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RolePermissionCrossRef(
    val roleId: Long,
    val flag: PermissionType
)
