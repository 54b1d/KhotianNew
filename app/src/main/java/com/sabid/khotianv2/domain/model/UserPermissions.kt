package com.sabid.khotianv2.domain.model

data class UserPermissions(
    val permissions: Set<PermissionType> = emptySet()
) {
    fun hasPermission(permission: PermissionType): Boolean = permissions.contains(permission)
    
    companion object {
        val NONE = UserPermissions()
        val ALL = UserPermissions(PermissionType.values().toSet())
    }
}
