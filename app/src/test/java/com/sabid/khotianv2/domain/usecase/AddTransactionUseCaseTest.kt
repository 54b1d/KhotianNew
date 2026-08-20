package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.FinancialAccountRepository
import com.sabid.khotianv2.domain.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class AddTransactionUseCaseTest {
    private val transactionRepository = mockk<TransactionRepository>()
    private val financialAccountRepository = mockk<FinancialAccountRepository>()
    private val permissionManager = mockk<PermissionManager>()
    private val sessionManager = mockk<SessionManager>()
    private lateinit var addTransactionUseCase: AddTransactionUseCase

    @Before
    fun setup() {
        addTransactionUseCase = AddTransactionUseCase(
            transactionRepository,
            financialAccountRepository,
            permissionManager,
            sessionManager
        )
        every { permissionManager.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS) } returns true
        every { sessionManager.currentUserId } returns MutableStateFlow("user123")
    }

    @Test
    fun `transfer transaction updates both account balances`() = runTest {
        val amount = BigDecimal("100.00")
        val fromAccountId = 1L
        val toAccountId = 2L
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs
        coEvery { financialAccountRepository.transferBalance(any(), any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            financialAccountId = fromAccountId,
            toFinancialAccountId = toAccountId,
            businessType = BusinessTransactionType.TRANSFER,
            note = "ATM Withdrawal"
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            financialAccountRepository.transferBalance(fromAccountId, toAccountId, amount)
        }
    }

    @Test
    fun `transfer between same account fails`() = runTest {
        val amount = BigDecimal("100.00")
        val accountId = 1L

        val result = addTransactionUseCase(
            amount = amount,
            financialAccountId = accountId,
            toFinancialAccountId = accountId,
            businessType = BusinessTransactionType.TRANSFER,
            note = "Invalid Transfer"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("different") == true)
    }
}
