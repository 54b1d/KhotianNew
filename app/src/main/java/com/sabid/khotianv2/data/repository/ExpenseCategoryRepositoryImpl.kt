package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.dao.ExpenseCategoryDao
import com.sabid.khotianv2.domain.model.ExpenseCategory
import com.sabid.khotianv2.domain.repository.ExpenseCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseCategoryRepositoryImpl @Inject constructor(
    private val expenseCategoryDao: ExpenseCategoryDao
) : ExpenseCategoryRepository {
    override fun getAllCategories(): Flow<List<ExpenseCategory>> =
        expenseCategoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getCategoryById(id: Long): ExpenseCategory? =
        expenseCategoryDao.getCategoryById(id)?.toDomain()

    override suspend fun addCategory(category: ExpenseCategory): Long =
        expenseCategoryDao.insertCategory(category.toEntity())

    override suspend fun updateCategory(category: ExpenseCategory) =
        expenseCategoryDao.updateCategory(category.toEntity())

    override suspend fun deleteCategory(category: ExpenseCategory) =
        expenseCategoryDao.deleteCategory(category.toEntity())
}
