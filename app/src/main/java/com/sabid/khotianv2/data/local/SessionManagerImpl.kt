package com.sabid.khotianv2.data.local

import com.sabid.khotianv2.domain.manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerImpl @Inject constructor() : SessionManager {
    private val _currentUserId = MutableStateFlow<String?>(null)
    override val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    override fun startSession(userId: String) {
        _currentUserId.value = userId
    }

    override fun endSession() {
        _currentUserId.value = null
    }
}
