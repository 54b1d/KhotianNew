package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.presentation.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onAccountClick: (Long) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val homeData by viewModel.homeData.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showCashBottomSheet by remember { mutableStateOf(false) }
    var showBankBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Operations", style = MaterialTheme.typography.titleMedium) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Date Navigation
            DateNavigationHeader(
                selectedDate = selectedDate,
                onPrevious = { viewModel.onPreviousDay() },
                onNext = { viewModel.onNextDay() },
                onPickDate = { showDatePicker = true }
            )

            // Financial Summary Chips
            FinancialSummaryRow(
                cashTotal = homeData?.cashTotal ?: java.math.BigDecimal.ZERO,
                bankTotal = homeData?.bankTotal ?: java.math.BigDecimal.ZERO,
                onCashClick = { showCashBottomSheet = true },
                onBankClick = { showBankBottomSheet = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Transaction List
            TransactionList(
                transactions = homeData?.transactions ?: emptyList(),
                onTransactionClick = { onTransactionClick(it.transaction) }
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val newDate = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onDateSelected(newDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCashBottomSheet) {
        AccountListBottomSheet(
            title = "Cash Accounts",
            accounts = homeData?.cashAccounts ?: emptyList(),
            onDismiss = { showCashBottomSheet = false },
            onAccountClick = {
                onAccountClick(it)
                showCashBottomSheet = false
            }
        )
    }

    if (showBankBottomSheet) {
        AccountListBottomSheet(
            title = "Bank Accounts",
            accounts = homeData?.bankAccounts ?: emptyList(),
            onDismiss = { showBankBottomSheet = false },
            onAccountClick = {
                onAccountClick(it)
                showBankBottomSheet = false
            }
        )
    }
}

@Composable
private fun DateNavigationHeader(
    selectedDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous Day")
            }

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onPickDate)
                    .weight(1f),
                textAlign = TextAlign.Center
            )

            Row {
                IconButton(onClick = onPickDate) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = "Pick Date")
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next Day")
                }
            }
        }
    }
}

@Composable
private fun FinancialSummaryRow(
    cashTotal: java.math.BigDecimal,
    bankTotal: java.math.BigDecimal,
    onCashClick: () -> Unit,
    onBankClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AssistChip(
            onClick = onCashClick,
            label = { Text("Cash: ${cashTotal.toPlainString()}") },
            leadingIcon = { Icon(Icons.Rounded.Payments, null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.weight(1f)
        )
        AssistChip(
            onClick = onBankClick,
            label = { Text("Bank: ${bankTotal.toPlainString()}") },
            leadingIcon = { Icon(Icons.Rounded.AccountBalance, null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TransactionList(
    transactions: List<TransactionItem>,
    onTransactionClick: (TransactionItem) -> Unit
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions for this date", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(transactions) { item ->
                TransactionRow(item = item, onClick = { onTransactionClick(item) })
            }
        }
    }
}

@Composable
private fun TransactionRow(item: TransactionItem, onClick: () -> Unit) {
    val transaction = item.transaction
    val displayName = item.partyName ?: item.accountName ?: item.categoryName ?: "General"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Line 1: [Type Badge] [Party/Account Name] (Left) and [Amount] (Right, Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransactionTypeBadge(transaction.businessType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.toAccountName != null) {
                        Text(
                            text = " → ${item.toAccountName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (transaction.businessType == BusinessTransactionType.PARTY_SETTLEMENT && item.toPartyName != null) {
                        Text(
                            text = " → ${item.toPartyName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = transaction.amount.toPlainString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Line 2: [Product Name] • [Qty] [Unit] x [Rate] or [Category/Account Details]
            val line2Text = when (transaction.businessType) {
                BusinessTransactionType.PURCHASE, BusinessTransactionType.SALE -> {
                    val qty = transaction.quantity?.stripTrailingZeros()?.toPlainString() ?: "0"
                    val unit = item.unitName ?: ""
                    val rate = transaction.rate?.stripTrailingZeros()?.toPlainString() ?: "0"
                    val product = item.productName ?: "Unknown Product"
                    "$product • $qty $unit x $rate"
                }
                BusinessTransactionType.TRANSFER -> "From ${item.accountName} to ${item.toAccountName}"
                BusinessTransactionType.EXPENSE -> "${item.categoryName} paid from ${item.accountName}"
                BusinessTransactionType.PAYMENT_RECEIVED -> "Received in ${item.accountName}"
                BusinessTransactionType.PAYMENT_MADE -> "Paid from ${item.accountName}"
                BusinessTransactionType.STOCK_ADJUSTMENT -> {
                    val qty = transaction.quantity?.stripTrailingZeros()?.toPlainString() ?: "0"
                    val unit = item.unitName ?: ""
                    "${item.productName} • Adjustment: $qty $unit"
                }
                BusinessTransactionType.PARTY_SETTLEMENT -> {
                    "Settlement: ${item.partyName} to ${item.toPartyName}"
                }
            }
            Text(
                text = line2Text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 40.dp) // Align with text after badge
            )
        }
    }
}

@Composable
private fun TransactionTypeBadge(type: BusinessTransactionType) {
    val containerColor = when (type) {
        BusinessTransactionType.PURCHASE -> MaterialTheme.colorScheme.primaryContainer
        BusinessTransactionType.SALE -> MaterialTheme.colorScheme.secondaryContainer
        BusinessTransactionType.PAYMENT_RECEIVED -> MaterialTheme.colorScheme.tertiaryContainer
        BusinessTransactionType.PAYMENT_MADE -> MaterialTheme.colorScheme.surfaceVariant
        BusinessTransactionType.TRANSFER -> MaterialTheme.colorScheme.outlineVariant
        BusinessTransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer
        BusinessTransactionType.STOCK_ADJUSTMENT -> MaterialTheme.colorScheme.surfaceDim
        BusinessTransactionType.PARTY_SETTLEMENT -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when (type) {
        BusinessTransactionType.PURCHASE -> MaterialTheme.colorScheme.onPrimaryContainer
        BusinessTransactionType.SALE -> MaterialTheme.colorScheme.onSecondaryContainer
        BusinessTransactionType.PAYMENT_RECEIVED -> MaterialTheme.colorScheme.onTertiaryContainer
        BusinessTransactionType.PAYMENT_MADE -> MaterialTheme.colorScheme.onSurfaceVariant
        BusinessTransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
        BusinessTransactionType.EXPENSE -> MaterialTheme.colorScheme.onErrorContainer
        BusinessTransactionType.STOCK_ADJUSTMENT -> MaterialTheme.colorScheme.onSurface
        BusinessTransactionType.PARTY_SETTLEMENT -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = when (type) {
        BusinessTransactionType.PURCHASE -> "Purc"
        BusinessTransactionType.SALE -> "Sale"
        BusinessTransactionType.PAYMENT_MADE -> "Paym"
        BusinessTransactionType.PAYMENT_RECEIVED -> "Recv"
        BusinessTransactionType.TRANSFER -> "Trsf"
        BusinessTransactionType.EXPENSE -> "Exps"
        BusinessTransactionType.STOCK_ADJUSTMENT -> "Stok"
        BusinessTransactionType.PARTY_SETTLEMENT -> "Setl"
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier.width(32.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            modifier = Modifier.padding(vertical = 1.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountListBottomSheet(
    title: String,
    accounts: List<FinancialAccount>,
    onDismiss: () -> Unit,
    onAccountClick: (Long) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            LazyColumn {
                items(accounts) { account ->
                    ListItem(
                        headlineContent = { Text(account.name) },
                        trailingContent = {
                            Text(
                                account.currentBalance.toPlainString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.clickable { onAccountClick(account.id) }
                    )
                }
            }
        }
    }
}
