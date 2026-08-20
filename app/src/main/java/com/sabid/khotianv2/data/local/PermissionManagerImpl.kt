package com.sabid.khotianv2.data.local

import com.sabid.khotianv2.data.local.dao.UserDao
import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.UserPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManagerImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : PermissionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _userPermissions = MutableStateFlow(UserPermissions.NONE)
    override val userPermissions: StateFlow<UserPermissions> = _userPermissions.asStateFlow()

    init {
        scope.launch {
            sessionManager.currentUserId.collectLatest { userId ->
                val longId = userId?.toLongOrNull()
                if (longId != null) {
                    userDao.getUserPermissions(longId).collect { permissions ->
                        _userPermissions.value = UserPermissions(permissions.toSet())
                    }
                } else {
                    _userPermissions.value = UserPermissions.NONE
                }
            }
        }
    }
}
