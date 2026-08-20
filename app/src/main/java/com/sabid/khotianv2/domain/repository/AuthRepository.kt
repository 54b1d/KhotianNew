package com.sabid.khotianv2.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val userId: Flow<String?>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getSession(): String?
}
