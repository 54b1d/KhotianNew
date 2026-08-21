package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.model.FinancialAccountType
import com.sabid.khotianv2.domain.model.HomeData
import com.sabid.khotianv2.domain.model.TransactionItem
import com.sabid.khotianv2.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: FinancialAccountRepository,
    private val businessRepository: BusinessRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val unitRepository: UnitRepository,
    private val productRepository: ProductRepository
) {
    operator fun invoke(date: LocalDate): Flow<HomeData> {
        val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val transactionsFlow = transactionRepository.getTransactionsByDate(startTime, endTime)
        val cashAccountsFlow = accountRepository.getAccountsByType(FinancialAccountType.CASH)
        val bankAccountsFlow = accountRepository.getAccountsByType(FinancialAccountType.BANK)
        val allAccountsFlow = accountRepository.getAllAccounts()
        val partiesFlow = businessRepository.getParties()
        val categoriesFlow = expenseCategoryRepository.getAllCategories()
        val unitsFlow = unitRepository.getAllUnits()
        val productsFlow = productRepository.getAllProducts()

        val metadataFlow = combine(allAccountsFlow, partiesFlow, categoriesFlow, unitsFlow, productsFlow) { accounts, parties, categories, units, products ->
            Metadata(
                accountMap = accounts.associateBy { it.id },
                partyMap = parties.associateBy { it.id },
                categoryMap = categories.associateBy { it.id },
                unitMap = units.associateBy { it.id },
                productMap = products.associateBy { it.id }
            )
        }

        return combine(
            transactionsFlow,
            cashAccountsFlow,
            bankAccountsFlow,
            metadataFlow
        ) { transactions, cashAccounts, bankAccounts, metadata ->
            val transactionItems = transactions.map { transaction ->
                TransactionItem(
                    transaction = transaction,
                    partyName = transaction.partyId?.let { metadata.partyMap[it]?.name },
                    toPartyName = transaction.toPartyId?.let { metadata.partyMap[it]?.name },
                    accountName = transaction.financialAccountId?.let { metadata.accountMap[it]?.name },
                    toAccountName = transaction.toFinancialAccountId?.let { metadata.accountMap[it]?.name },
                    categoryName = transaction.expenseCategoryId?.let { metadata.categoryMap[it]?.name },
                    unitName = transaction.unitId?.let { metadata.unitMap[it]?.symbol ?: metadata.unitMap[it]?.name },
                    productName = transaction.productId?.let { metadata.productMap[it]?.name }
                )
            }

            HomeData(
                selectedDate = date,
                transactions = transactionItems,
                cashAccounts = cashAccounts,
                bankAccounts = bankAccounts,
                cashTotal = cashAccounts.sumOfBigDecimal { it.currentBalance },
                bankTotal = bankAccounts.sumOfBigDecimal { it.currentBalance }
            )
        }
    }

    private data class Metadata(
        val accountMap: Map<Long, com.sabid.khotianv2.domain.model.FinancialAccount>,
        val partyMap: Map<Long, com.sabid.khotianv2.domain.model.Party>,
        val categoryMap: Map<Long, com.sabid.khotianv2.domain.model.ExpenseCategory>,
        val unitMap: Map<Long, com.sabid.khotianv2.domain.model.AppUnit>,
        val productMap: Map<Long, com.sabid.khotianv2.domain.model.Product>
    )

    private inline fun <T> Iterable<T>.sumOfBigDecimal(selector: (T) -> BigDecimal): BigDecimal {
        var sum = BigDecimal.ZERO
        for (element in this) {
            sum = sum.add(selector(element))
        }
        return sum
    }
}
