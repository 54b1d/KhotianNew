package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.presentation.viewmodel.FinancialAccountLedgerViewModel
import com.sabid.khotianv2.presentation.viewmodel.TransactionItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAccountLedgerScreen(
    viewModel: FinancialAccountLedgerViewModel,
    onBackClick: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val account by viewModel.account.collectAsState()
    val allAccounts by viewModel.accounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(account?.name ?: "Account Ledger", style = MaterialTheme.typography.titleMedium)
                        if (account != null) {
                            Text(
                                "Balance: ${account?.currentBalance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(transactions) { item ->
                AccountTransactionRow(item, account?.id ?: 0L, allAccounts, onClick = { onTransactionClick(item.transaction.id) })
            }
        }
    }
}

@Composable
fun AccountTransactionRow(item: TransactionItem, currentAccountId: Long, allAccounts: List<FinancialAccount>, onClick: () -> Unit) {
    val transaction = item.transaction
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(transaction.timestamp))
    
    val isTransfer = transaction.businessType == BusinessTransactionType.TRANSFER
    val isTransferIn = isTransfer && transaction.toFinancialAccountId == currentAccountId
    
    val title = if (isTransfer) {
        if (isTransferIn) {
            val fromAccount = allAccounts.find { it.id == transaction.financialAccountId }?.name ?: "Unknown"
            "Transfer In from $fromAccount"
        } else {
            val toAccount = allAccounts.find { it.id == transaction.toFinancialAccountId }?.name ?: "Unknown"
            "Transfer Out to $toAccount"
        }
    } else {
        transaction.businessType.name.replace("_", " ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val isPositive = transaction.businessType == BusinessTransactionType.PAYMENT_RECEIVED || isTransferIn
                val color = if (isPositive)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                
                Text(
                    text = if (isPositive) 
                        "+${transaction.amount}" else "-${transaction.amount}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "Bal: ${item.runningBalance}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
