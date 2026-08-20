package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.presentation.viewmodel.ProfitLossViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossScreen(
    viewModel: ProfitLossViewModel,
    onBack: () -> Unit,
    onStocktake: () -> Unit
) {
    val report by viewModel.report.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profit & Loss Report", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onStocktake) {
                        Text("Inventory Check")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Profit Card
            val isProfit = report.netProfit >= BigDecimal.ZERO
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isProfit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProfit) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isProfit) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "৳ ${report.netProfit.toPlainString()}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProfit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Trading Account Section
            Text("Trading Account (Stock Valuation)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            
            ReportItem(label = "Opening Stock Value", amount = report.openingStockValue)
            ReportItem(label = "Total Purchases", amount = report.totalPurchases)
            ReportItem(label = "Closing Stock Value", amount = report.closingStockValue, color = Color(0xFF2E7D32))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            ReportItem(
                label = "Cost of Goods Sold (COGS)", 
                amount = report.costOfGoodsSold, 
                color = MaterialTheme.colorScheme.error,
                note = "(Opening + Purchases) - Closing"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            ReportItem(
                label = "Total Sales (Revenue)", 
                amount = report.totalSales, 
                color = Color(0xFF2E7D32)
            )
            
            ReportItem(
                label = "Gross Profit", 
                amount = report.grossProfit, 
                isBold = true,
                color = if (report.grossProfit >= BigDecimal.ZERO) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                note = "Sales - COGS"
            )

            Spacer(Modifier.height(8.dp))
            Text("Operating Expenses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            if (report.expensesByCategory.isEmpty()) {
                Text(
                    "No expenses recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                report.expensesByCategory.forEach { (category, amount) ->
                    ReportItem(label = category, amount = amount)
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            ReportItem(label = "Total Expenses", amount = report.totalExpenses, isBold = true, color = MaterialTheme.colorScheme.error)

            Spacer(Modifier.height(16.dp))
            
            // Summary Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Net Profit Calculation: Gross Profit - Total Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReportItem(
    label: String,
    amount: BigDecimal,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface,
    note: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "৳ ${amount.toPlainString()}",
                style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
                color = color
            )
        }
        if (note != null) {
            Text(
                text = note,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
