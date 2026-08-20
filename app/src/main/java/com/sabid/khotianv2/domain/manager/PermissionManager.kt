package com.sabid.khotianv2.domain.manager

import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.model.UserPermissions
import kotlinx.coroutines.flow.StateFlow

interface PermissionManager {
    val userPermissions: StateFlow<UserPermissions>
    
    fun hasPermission(permission: PermissionType): Boolean {
        return userPermissions.value.hasPermission(permission)
    }
}
