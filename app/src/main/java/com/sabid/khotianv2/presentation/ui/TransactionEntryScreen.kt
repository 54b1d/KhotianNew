package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.FreightType
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.presentation.viewmodel.TransactionEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    viewModel: TransactionEntryViewModel,
    onSuccess: () -> Unit,
    onAddPartyClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onBack: () -> Unit
) {
    val parties by viewModel.parties.collectAsState()
    val products by viewModel.products.collectAsState()
    val units by viewModel.units.collectAsState()
    val permissions by viewModel.userPermissions.collectAsState()
    val financialAccounts by viewModel.financialAccounts.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    
    LaunchedEffect(viewModel) {
        viewModel.loadTransaction(viewModel.transactionId)
    }

    var showPartyMenu by remember { mutableStateOf(false) }
    var showProductMenu by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showExpenseCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (viewModel.isEditing) "Edit Transaction" else "New Transaction", 
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.submitTransaction(onSuccess, {}) },
                icon = { Icon(Icons.Rounded.Check, contentDescription = null) },
                text = { Text(if (viewModel.isEditing) "Update" else "Save") },
                expanded = !viewModel.isSubmitting
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Transaction Type Selection
            Text("Transaction Type", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                BusinessTransactionType.entries.forEachIndexed { index, type ->
                    val label = when (type) {
                        BusinessTransactionType.PURCHASE -> "Purc"
                        BusinessTransactionType.SALE -> "Sale"
                        BusinessTransactionType.PAYMENT_MADE -> "Paid"
                        BusinessTransactionType.PAYMENT_RECEIVED -> "Recv"
                        BusinessTransactionType.TRANSFER -> "Xfer"
                        BusinessTransactionType.EXPENSE -> "Expn"
                        BusinessTransactionType.STOCK_ADJUSTMENT -> "Adj"
                        BusinessTransactionType.PARTY_SETTLEMENT -> "Setl"
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = BusinessTransactionType.entries.size),
                        onClick = { viewModel.businessType = type },
                        selected = viewModel.businessType == type,
                        label = { 
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            ) 
                        }
                    )
                }
            }

            // Party Selection
            if (viewModel.businessType != BusinessTransactionType.TRANSFER && 
                viewModel.businessType != BusinessTransactionType.EXPENSE) {
                ExposedDropdownMenuBox(
                    expanded = showPartyMenu,
                    onExpandedChange = { showPartyMenu = it }
                ) {
                    OutlinedTextField(
                        value = parties.find { it.id == viewModel.partyId }?.name ?: "Select Party",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (viewModel.businessType == BusinessTransactionType.PARTY_SETTLEMENT) "From Party (Payer)" else "Party") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPartyMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showPartyMenu,
                        onDismissRequest = { showPartyMenu = false }
                    ) {
                        parties.forEach { party ->
                            DropdownMenuItem(
                                text = { Text(party.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    viewModel.partyId = party.id
                                    showPartyMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add New Party", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                showPartyMenu = false
                                onAddPartyClick()
                            }
                        )
                    }
                }

                if (viewModel.businessType == BusinessTransactionType.PARTY_SETTLEMENT) {
                    var showToPartyMenu by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showToPartyMenu,
                        onExpandedChange = { showToPartyMenu = it }
                    ) {
                        OutlinedTextField(
                            value = parties.find { it.id == viewModel.toPartyId }?.name ?: "Select Party",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Party (Receiver)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToPartyMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showToPartyMenu,
                            onDismissRequest = { showToPartyMenu = false }
                        ) {
                            parties.filter { it.id != viewModel.partyId }.forEach { party ->
                                DropdownMenuItem(
                                    text = { Text(party.name, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        viewModel.toPartyId = party.id
                                        showToPartyMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Product Selection (for Purchase/Sale/Stock Adjustment)
            if (viewModel.businessType == BusinessTransactionType.PURCHASE || 
                viewModel.businessType == BusinessTransactionType.SALE ||
                viewModel.businessType == BusinessTransactionType.STOCK_ADJUSTMENT) {
                
                ExposedDropdownMenuBox(
                    expanded = showProductMenu,
                    onExpandedChange = { showProductMenu = it }
                ) {
                    OutlinedTextField(
                        value = products.find { it.id == viewModel.productId }?.name ?: "Select Product",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showProductMenu,
                        onDismissRequest = { showProductMenu = false }
                    ) {
                        products.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    viewModel.productId = product.id
                                    // Set default unit if available
                                    product.defaultUnitId?.let { viewModel.unitId = it }
                                    showProductMenu = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add New Product", style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            onClick = {
                                showProductMenu = false
                                onAddProductClick()
                            }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.quantity,
                        onValueChange = { viewModel.quantity = it },
                        label = { Text("Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showUnitMenu,
                        onExpandedChange = { showUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = units.find { it.id == viewModel.unitId }?.symbol ?: "Unit",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})", style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        viewModel.unitId = unit.id
                                        showUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = viewModel.rate,
                    onValueChange = { viewModel.rate = it },
                    label = { Text("Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            } else if (viewModel.businessType == BusinessTransactionType.EXPENSE) {
                // Expense Category Selection
                ExposedDropdownMenuBox(
                    expanded = showExpenseCategoryMenu,
                    onExpandedChange = { showExpenseCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = expenseCategories.find { it.id == viewModel.expenseCategoryId }?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showExpenseCategoryMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showExpenseCategoryMenu,
                        onDismissRequest = { showExpenseCategoryMenu = false }
                    ) {
                        expenseCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    viewModel.expenseCategoryId = category.id
                                    showExpenseCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = { viewModel.amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            } else if (viewModel.businessType == BusinessTransactionType.PARTY_SETTLEMENT) {
                // For settlement, only show Amount (already covered by party selection above)
                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = { viewModel.amountText = it },
                    label = { Text("Settlement Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            } else {
                // Just Amount for Payments
                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = { viewModel.amountText = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Financial Account Selection (Payment Method / Transfer / Expense)
            if (viewModel.businessType == BusinessTransactionType.PAYMENT_MADE ||
                viewModel.businessType == BusinessTransactionType.PAYMENT_RECEIVED ||
                viewModel.businessType == BusinessTransactionType.TRANSFER ||
                viewModel.businessType == BusinessTransactionType.EXPENSE) {
                
                var showToAccountMenu by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.businessType == BusinessTransactionType.TRANSFER) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Contra Entry Details",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = showAccountMenu,
                        onExpandedChange = { showAccountMenu = it }
                    ) {
                        OutlinedTextField(
                            value = financialAccounts.find { it.id == viewModel.financialAccountId }?.name ?: "Select ${if (viewModel.businessType == BusinessTransactionType.TRANSFER) "Source" else "Payment"} Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (viewModel.businessType == BusinessTransactionType.TRANSFER) "From (Source) Account" else "Payment Method (Account)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false }
                        ) {
                            financialAccounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(account.name, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                account.type.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.financialAccountId = account.id
                                        showAccountMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (viewModel.businessType == BusinessTransactionType.TRANSFER) {
                        ExposedDropdownMenuBox(
                            expanded = showToAccountMenu,
                            onExpandedChange = { showToAccountMenu = it }
                        ) {
                            OutlinedTextField(
                                value = financialAccounts.find { it.id == viewModel.toFinancialAccountId }?.name ?: "Select Destination Account",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To (Destination) Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToAccountMenu) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            ExposedDropdownMenu(
                                expanded = showToAccountMenu,
                                onDismissRequest = { showToAccountMenu = false }
                            ) {
                                financialAccounts.forEach { account ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(account.name, style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    account.type.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.toFinancialAccountId = account.id
                                            showToAccountMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Freight Section
            if (viewModel.businessType == BusinessTransactionType.PURCHASE || 
                viewModel.businessType == BusinessTransactionType.SALE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.freightAmountText,
                        onValueChange = { viewModel.freightAmountText = it },
                        label = { Text("Freight Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Freight Born By", style = MaterialTheme.typography.labelSmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = viewModel.freightType == FreightType.BORN_BY_US,
                                onClick = { viewModel.freightType = FreightType.BORN_BY_US }
                            )
                            Text("Us", style = MaterialTheme.typography.labelSmall)
                            RadioButton(
                                selected = viewModel.freightType == FreightType.BORN_BY_SELLER,
                                onClick = { viewModel.freightType = FreightType.BORN_BY_SELLER }
                            )
                            Text("Other", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (viewModel.businessType != BusinessTransactionType.TRANSFER) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net Amount", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            viewModel.netCost.toPlainString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transfer Amount", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            viewModel.amount.toPlainString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.note,
                onValueChange = { viewModel.note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                textStyle = MaterialTheme.typography.bodySmall
            )

            if (permissions.hasPermission(PermissionType.CAN_DELETE_TRANSACTIONS)) {
                Text(
                    "Admin Actions Enabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (viewModel.isSubmitting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
