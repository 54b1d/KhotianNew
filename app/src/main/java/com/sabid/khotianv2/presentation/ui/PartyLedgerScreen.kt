package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.Transaction
import com.sabid.khotianv2.domain.model.TransactionType
import com.sabid.khotianv2.presentation.viewmodel.LedgerViewModel
import com.sabid.khotianv2.presentation.viewmodel.TransactionItem
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyLedgerScreen(
    viewModel: LedgerViewModel,
    onBackClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onEditPartyClick: (Long) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val totalBalance by viewModel.balance.collectAsState()
    val party by viewModel.party.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var expandedTransactionId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(party?.name ?: "Party Ledger", style = MaterialTheme.typography.titleMedium)
                        if (party != null) {
                            Text(
                                party!!.type.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { party?.let { onEditPartyClick(it.id) } }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit Party")
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            "Total Bal: ৳ ${totalBalance.toPlainString()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (totalBalance >= BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Party Info Header
            if (party != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(8.dp)) {
                        if (!party!!.phoneNumber.isNullOrBlank()) {
                            Text("📞 ${party!!.phoneNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!party!!.address.isNullOrBlank()) {
                            Text("📍 ${party!!.address}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                
                Text(
                    text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }

            LedgerHeader()
            
            if (transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions for this month", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(transactions) { item ->
                        val isExpanded = expandedTransactionId == item.transaction.id
                        LedgerRow(
                            item = item, 
                            isExpanded = isExpanded,
                            onClick = { 
                                expandedTransactionId = if (isExpanded) null else item.transaction.id 
                            },
                            onEditClick = { onTransactionClick(item.transaction.id) }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText("Date", Modifier.weight(1.2f))
        HeaderText("Type", Modifier.weight(1f))
        HeaderText("Detail", Modifier.weight(1.5f))
        HeaderText("Amt/Qty", Modifier.weight(1.3f), textAlign = TextAlign.End)
        HeaderText("Balance", Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
private fun HeaderText(text: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        textAlign = textAlign
    )
}


@Composable
private fun LedgerRow(
    item: TransactionItem,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val transaction = item.transaction
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { SimpleDateFormat("dd/MM/yy", locale) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = dateFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
                Text(
                    text = "By: ${transaction.createdBy}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            val typeLabel = when (transaction.businessType) {
                BusinessTransactionType.PURCHASE -> "Purc"
                BusinessTransactionType.SALE -> "Sale"
                BusinessTransactionType.PAYMENT_MADE -> "PayM"
                BusinessTransactionType.PAYMENT_RECEIVED -> "PayR"
                BusinessTransactionType.TRANSFER -> "Trns"
                BusinessTransactionType.EXPENSE -> "Expn"
                BusinessTransactionType.STOCK_ADJUSTMENT -> "Adj"
                BusinessTransactionType.PARTY_SETTLEMENT -> "Setl"
                BusinessTransactionType.EQUITY_CONTRIBUTION -> "EqtC"
                BusinessTransactionType.EQUITY_WITHDRAWAL -> "EqtW"
                BusinessTransactionType.PROFIT_DISTRIBUTION -> "Prof"
            }
            val typeColor = if (item.isCredit) Color(0xFF2E7D32) else Color(0xFFC62828)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = typeColor
                )
                if (item.otherPartyName != null) {
                    Text(
                        text = item.otherPartyName,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 7.sp),
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1.5f)) {
                val detailText = when {
                    transaction.linkedTransactionType != null -> "Cost: ${transaction.linkedTransactionType.name}"
                    item.productName != null -> item.productName
                    else -> ""
                }
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.weight(1.3f), horizontalAlignment = Alignment.End) {
                if (transaction.quantity != null) {
                    Text(
                        text = "${transaction.quantity.toPlainString()} ${item.unitSymbol ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
                Text(
                    text = transaction.amount.toPlainString(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            Text(
                text = item.runningBalance.toPlainString(),
                modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.End,
                color = if (item.runningBalance >= BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828),
                maxLines = 1
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                if (!item.productName.isNullOrBlank()) {
                    Text(
                        text = "Product: ${item.productName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = "Note: ${transaction.note}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Created by: ${transaction.createdBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        val timeFormat = remember(locale) { SimpleDateFormat("hh:mm a", locale) }
                        Text(
                            text = "Time: ${timeFormat.format(Date(transaction.timestamp))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
