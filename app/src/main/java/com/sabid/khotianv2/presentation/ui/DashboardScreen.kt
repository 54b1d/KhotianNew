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
    onStocktakeClick: () -> Unit
) {
    val productStocks by viewModel.productStocks.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()
    val permissions by viewModel.userPermissions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleMedium) }
            )
        },
        floatingActionButton = {
            if (permissions.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
                FloatingActionButton(
                    onClick = onAddTransactionClick,
                    modifier = Modifier.imePadding()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
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
            // Management Tools Section
            if (permissions.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Management Tools",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                item {
                    ActionCard(
                        title = "Crushing",
                        icon = Icons.Rounded.Factory,
                        onClick = onCrushingEntryClick,
                        enabled = permissions.hasPermission(PermissionType.CAN_MANAGE_FACTORY)
                    )
                }
                item {
                    ActionCard(
                        title = "Accounts",
                        icon = Icons.Rounded.AccountBalance,
                        onClick = onManageAccountsClick
                    )
                }
                item {
                    ActionCard(
                        title = "Add Party",
                        icon = Icons.Rounded.PersonAdd,
                        onClick = onAddPartyClick
                    )
                }
                item {
                    ActionCard(
                        title = "Profit & Loss",
                        icon = Icons.Rounded.Assessment,
                        onClick = onProfitLossClick
                    )
                }
                item {
                    ActionCard(
                        title = "Inventory",
                        icon = Icons.Rounded.Inventory,
                        onClick = onStocktakeClick
                    )
                }
                item {
                    ActionCard(
                        title = "Backup",
                        icon = Icons.Rounded.Backup,
                        onClick = onBackupClick
                    )
                }
                item {
                    ActionCard(
                        title = "Settings",
                        icon = Icons.Rounded.Settings,
                        onClick = onUnitManagementClick
                    )
                }
            }

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
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
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
