package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.repository.ProductStock
import com.sabid.khotianv2.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onFinancialAccountClick: (Long) -> Unit,
    onManageAccountsClick: () -> Unit,
    onAddPartyClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onCrushingEntryClick: () -> Unit,
    onUnitManagementClick: () -> Unit,
    onBackupClick: () -> Unit,
    onProfitLossClick: () -> Unit,
    onStocktakeClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val productStocks by viewModel.productStocks.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()
    val permissions by viewModel.userPermissions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (permissions.hasPermission(PermissionType.CAN_MANAGE_FACTORY)) {
                    ExtendedFloatingActionButton(
                        onClick = onCrushingEntryClick,
                        icon = { Icon(Icons.Rounded.Factory, null) },
                        text = { Text("Crushing") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                if (permissions.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
                    SmallFloatingActionButton(
                        onClick = onManageAccountsClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Rounded.AccountBalance, contentDescription = "Accounts")
                    }
                    SmallFloatingActionButton(
                        onClick = onBackupClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Rounded.Backup, contentDescription = "Backup")
                    }
                    SmallFloatingActionButton(
                        onClick = onProfitLossClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Rounded.Assessment, contentDescription = "Profit & Loss")
                    }
                    SmallFloatingActionButton(
                        onClick = onStocktakeClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Rounded.Inventory, contentDescription = "Inventory Check")
                    }
                    SmallFloatingActionButton(
                        onClick = onUnitManagementClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Units")
                    }
                    SmallFloatingActionButton(
                        onClick = onAddPartyClick,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Party")
                    }
                    FloatingActionButton(onClick = onAddTransactionClick) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            if (financialAccounts.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Cash & Bank",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(financialAccounts) { account ->
                    AccountCard(account = account, onClick = { onFinancialAccountClick(account.id) })
                }
            }

            if (productStocks.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Product Stock",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(productStocks) { stock ->
                    StockCard(stock = stock)
                }
            }
        }
    }
}

@Composable
private fun AccountCard(account: com.sabid.khotianv2.domain.model.FinancialAccount, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (account.type == com.sabid.khotianv2.domain.model.FinancialAccountType.CASH) 
                        Icons.Rounded.Payments else Icons.Rounded.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = account.currentBalance.toPlainString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun StockCard(stock: ProductStock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = stock.productName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val stockText = if (stock.stockInDefaultUnit != null) {
                "${stock.stockInDefaultUnit.toPlainString()} ${stock.defaultUnitSymbol ?: ""}"
            } else {
                "${stock.baseStock.toPlainString()} kg"
            }
            Text(
                text = stockText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
