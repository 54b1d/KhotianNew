package com.sabid.khotianv2.domain.repository

import com.sabid.khotianv2.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

interface ExpenseCategoryRepository {
    fun getAllCategories(): Flow<List<ExpenseCategory>>
    suspend fun getCategoryById(id: Long): ExpenseCategory?
    suspend fun addCategory(category: ExpenseCategory): Long
    suspend fun updateCategory(category: ExpenseCategory)
    suspend fun deleteCategory(category: ExpenseCategory)
}
