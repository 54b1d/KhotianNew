package com.sabid.khotianv2.domain.manager

import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val currentUserId: StateFlow<String?>
    fun startSession(userId: String)
    fun endSession()
}
