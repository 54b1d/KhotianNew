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
        coEvery { transactionRepository.getTransactionById(any()) } returns null
        coEvery { transactionRepository.getChildTransactions(any()) } returns emptyList()
        @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
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
            @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
}
        @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
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
        @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
}

    @Test
    fun `purchase transaction with additional cost updates both party and cost accounts`() = runTest {
        val amount = BigDecimal("1000.00")
        val partyId = 1L
        val freightAmount = BigDecimal("50.00")
        val cashAccountId = 1L
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            partyId = partyId,
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Freight",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.FREIGHT,
                    amount = freightAmount,
                    financialAccountId = cashAccountId, // Paid by us from Cash
                    toPartyId = 2L // Paid to a transporter
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Main transaction
            transactionRepository.addTransaction(match { it.amount == amount && it.businessType == BusinessTransactionType.PURCHASE })
            // Child transaction for freight
            transactionRepository.addTransaction(match { it.amount == freightAmount && it.linkedTransactionType == LinkedTransactionType.FREIGHT })
            // Balance update for freight payment
            financialAccountRepository.updateBalance(cashAccountId, freightAmount.negate())
            @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
}
        @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
}
    @Test
    fun `commission payable to party increases their balance`() = runTest {
        val amount = BigDecimal("1000.00")
        val brokerPartyId = 3L
        val commissionAmount = BigDecimal("100.00")
        
        coEvery { transactionRepository.addTransaction(any()) } returns 1L
        coEvery { financialAccountRepository.updateBalance(any(), any()) } just runs

        val result = addTransactionUseCase(
            amount = amount,
            businessType = BusinessTransactionType.PURCHASE,
            note = "Purchase with Commission",
            additionalCosts = listOf(
                AdditionalCost(
                    type = LinkedTransactionType.COMMISSION,
                    amount = commissionAmount,
                    toPartyId = brokerPartyId // Payable to broker
                )
            )
        )

        assertTrue(result.isSuccess)
        
        coVerify {
            // Child transaction for commission (CREDIT)
            transactionRepository.addTransaction(match { 
                it.amount == commissionAmount && 
                it.linkedTransactionType == LinkedTransactionType.COMMISSION &&
                it.type == TransactionType.CREDIT &&
                it.partyId == brokerPartyId
            })
        }
    }
}
