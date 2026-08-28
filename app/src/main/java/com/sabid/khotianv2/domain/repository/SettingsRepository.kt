package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.CommaStyle
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCommaStyle(): Flow<CommaStyle>
    suspend fun setCommaStyle(style: CommaStyle)
}
