package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.sabid.khotianv2.domain.model.BusinessTransactionType
import com.sabid.khotianv2.domain.model.LinkedTransactionType
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.presentation.viewmodel.CostRow
import com.sabid.khotianv2.presentation.viewmodel.TransactionEntryViewModel
import com.sabid.khotianv2.presentation.viewmodel.UnifiedAccount

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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    viewModel.submitTransaction(
                        onSuccess = onSuccess,
                        onError = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    ) 
                },
                icon = { Icon(Icons.Rounded.Check, contentDescription = null) },
                text = { Text(if (viewModel.isEditing) "Update" else "Save") },
                expanded = !viewModel.isSubmitting,
                modifier = Modifier.imePadding()
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
            val productTypes = remember {
                listOf(
                    BusinessTransactionType.PURCHASE,
                    BusinessTransactionType.SALE,
                    BusinessTransactionType.STOCK_ADJUSTMENT
                )
            }
            val otherTypes = remember {
                listOf(
                    BusinessTransactionType.TRANSFER,
                    BusinessTransactionType.EXPENSE,
                    BusinessTransactionType.PROFIT_DISTRIBUTION
                )
            }

            Text("Product Transactions", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                productTypes.forEachIndexed { index, type ->
                    val label = when (type) {
                        BusinessTransactionType.PURCHASE -> "Purchase"
                        BusinessTransactionType.SALE -> "Sale"
                        BusinessTransactionType.STOCK_ADJUSTMENT -> "Adjustment"
                        else -> ""
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = productTypes.size),
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

            Text("Other Transactions", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                otherTypes.forEachIndexed { index, type ->
                    val label = when (type) {
                        BusinessTransactionType.TRANSFER -> "Transfer"
                        BusinessTransactionType.EXPENSE -> "Expense"
                        BusinessTransactionType.PROFIT_DISTRIBUTION -> "Profit"
                        else -> ""
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = otherTypes.size),
                        onClick = { viewModel.businessType = type },
                        selected = viewModel.isTransferMode && type == BusinessTransactionType.TRANSFER || (!viewModel.isTransferMode && viewModel.businessType == type),
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

            // Unified Transfer Selection
            if (viewModel.isTransferMode) {
                val unifiedAccounts by viewModel.unifiedAccounts.collectAsState()
                var showSrcMenu by remember { mutableStateOf(false) }
                var showDestMenu by remember { mutableStateOf(false) }

                var srcSearchQuery by remember { mutableStateOf(viewModel.sourceAccount?.name ?: "") }
                var destSearchQuery by remember { mutableStateOf(viewModel.destinationAccount?.name ?: "") }

                LaunchedEffect(viewModel.sourceAccount) {
                    srcSearchQuery = viewModel.sourceAccount?.name ?: ""
                }
                LaunchedEffect(viewModel.destinationAccount) {
                    destSearchQuery = viewModel.destinationAccount?.name ?: ""
                }

                val filteredSrcAccounts = remember(unifiedAccounts, srcSearchQuery) {
                    if (srcSearchQuery.isBlank()) unifiedAccounts
                    else unifiedAccounts.filter { it.name.contains(srcSearchQuery, ignoreCase = true) }
                }

                val filteredDestAccounts = remember(unifiedAccounts, destSearchQuery, viewModel.sourceAccount) {
                    val list = unifiedAccounts.filter { 
                        val sameId = it.id == viewModel.sourceAccount?.id
                        val sameType = (it is UnifiedAccount.Financial && viewModel.sourceAccount is UnifiedAccount.Financial) ||
                                       (it is UnifiedAccount.PartyAccount && viewModel.sourceAccount is UnifiedAccount.PartyAccount)
                        !(sameId && sameType)
                    }
                    if (destSearchQuery.isBlank()) list
                    else list.filter { it.name.contains(destSearchQuery, ignoreCase = true) }
                }

                ExposedDropdownMenuBox(
                    expanded = showSrcMenu,
                    onExpandedChange = { showSrcMenu = it }
                ) {
                    OutlinedTextField(
                        value = srcSearchQuery,
                        onValueChange = { 
                            srcSearchQuery = it
                            showSrcMenu = true
                        },
                        readOnly = false,
                        label = { Text("From Account / Party *") },
                        isError = viewModel.showErrors && viewModel.sourceAccount == null,
                        trailingIcon = {
                            Row {
                                if (srcSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        srcSearchQuery = ""
                                        viewModel.onSourceAccountSelected(null)
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSrcMenu)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showSrcMenu,
                        onDismissRequest = { showSrcMenu = false }
                    ) {
                        filteredSrcAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(account.name, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            if (account is UnifiedAccount.Financial) "Account" else "Party",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onSourceAccountSelected(account)
                                    srcSearchQuery = account.name
                                    showSrcMenu = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = showDestMenu,
                    onExpandedChange = { showDestMenu = it }
                ) {
                    OutlinedTextField(
                        value = destSearchQuery,
                        onValueChange = { 
                            destSearchQuery = it
                            showDestMenu = true
                        },
                        readOnly = false,
                        label = { Text("To Account / Party *") },
                        isError = viewModel.showErrors && viewModel.destinationAccount == null,
                        trailingIcon = {
                            Row {
                                if (destSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        destSearchQuery = ""
                                        viewModel.onDestinationAccountSelected(null)
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDestMenu)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showDestMenu,
                        onDismissRequest = { showDestMenu = false }
                    ) {
                        filteredDestAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(account.name, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            if (account is UnifiedAccount.Financial) "Account" else "Party",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onDestinationAccountSelected(account)
                                    destSearchQuery = account.name
                                    showDestMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.amountText,
                    onValueChange = { viewModel.amountText = it },
                    label = { Text("Amount *") },
                    isError = viewModel.showErrors && (viewModel.amountText.isEmpty() || viewModel.amountText == "0"),
                    trailingIcon = {
                        if (viewModel.amountText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.amountText = "0" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { 
                        if (it.isFocused && viewModel.amountText == "0") viewModel.amountText = ""
                    },
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }

            // Party Selection
            if (!viewModel.isTransferMode && viewModel.businessType != BusinessTransactionType.EXPENSE) {
                var partySearchQuery by remember { mutableStateOf(parties.find { it.id == viewModel.partyId }?.name ?: "") }
                LaunchedEffect(viewModel.partyId, parties) {
                    partySearchQuery = parties.find { it.id == viewModel.partyId }?.name ?: ""
                }
                val filteredPartiesList = remember(parties, partySearchQuery, viewModel.businessType) {
                    val list = if (viewModel.businessType == BusinessTransactionType.PROFIT_DISTRIBUTION) {
                        parties.filter { it.type == "PARTNER" }
                    } else parties
                    
                    if (partySearchQuery.isBlank()) list
                    else list.filter { it.name.contains(partySearchQuery, ignoreCase = true) }
                }

                ExposedDropdownMenuBox(
                    expanded = showPartyMenu,
                    onExpandedChange = { showPartyMenu = it }
                ) {
                    OutlinedTextField(
                        value = partySearchQuery,
                        onValueChange = { 
                            partySearchQuery = it
                            showPartyMenu = true
                        },
                        readOnly = false,
                        label = { Text(if (viewModel.businessType == BusinessTransactionType.PARTY_SETTLEMENT) "From Party (Payer) *" else "Party *") },
                        isError = viewModel.showErrors && (viewModel.partyId == null || viewModel.partyId == 0L) && viewModel.businessType != BusinessTransactionType.STOCK_ADJUSTMENT,
                        trailingIcon = {
                            Row {
                                if (partySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        partySearchQuery = ""
                                        viewModel.partyId = 0L
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPartyMenu)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showPartyMenu,
                        onDismissRequest = { showPartyMenu = false }
                    ) {
                        filteredPartiesList.forEach { party ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(party.name, style = MaterialTheme.typography.bodySmall)
                                        if (party.type == "PARTNER") {
                                            Text("Partner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.partyId = party.id
                                    partySearchQuery = party.name
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
                    var toPartySearchQuery by remember { mutableStateOf(parties.find { it.id == viewModel.toPartyId }?.name ?: "") }
                    LaunchedEffect(viewModel.toPartyId, parties) {
                        toPartySearchQuery = parties.find { it.id == viewModel.toPartyId }?.name ?: ""
                    }
                    val filteredToParties = remember(parties, toPartySearchQuery, viewModel.partyId) {
                        val list = parties.filter { it.id != viewModel.partyId }
                        if (toPartySearchQuery.isBlank()) list
                        else list.filter { it.name.contains(toPartySearchQuery, ignoreCase = true) }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = showToPartyMenu,
                        onExpandedChange = { showToPartyMenu = it }
                    ) {
                        OutlinedTextField(
                            value = toPartySearchQuery,
                            onValueChange = { 
                                toPartySearchQuery = it
                                showToPartyMenu = true
                            },
                            readOnly = false,
                            label = { Text("To Party (Receiver) *") },
                            isError = viewModel.showErrors && (viewModel.toPartyId == null || viewModel.toPartyId == 0L),
                            trailingIcon = {
                                Row {
                                    if (toPartySearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            toPartySearchQuery = ""
                                            viewModel.toPartyId = 0L
                                        }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToPartyMenu)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showToPartyMenu,
                            onDismissRequest = { showToPartyMenu = false }
                        ) {
                            filteredToParties.forEach { party ->
                                DropdownMenuItem(
                                    text = { Text(party.name, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        viewModel.toPartyId = party.id
                                        toPartySearchQuery = party.name
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
                
                var productSearchQuery by remember { mutableStateOf(products.find { it.id == viewModel.productId }?.name ?: "") }
                LaunchedEffect(viewModel.productId, products) {
                    productSearchQuery = products.find { it.id == viewModel.productId }?.name ?: ""
                }
                val filteredProducts = remember(products, productSearchQuery) {
                    if (productSearchQuery.isBlank()) products
                    else products.filter { it.name.contains(productSearchQuery, ignoreCase = true) }
                }

                ExposedDropdownMenuBox(
                    expanded = showProductMenu,
                    onExpandedChange = { showProductMenu = it }
                ) {
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { 
                            productSearchQuery = it
                            showProductMenu = true
                        },
                        readOnly = false,
                        label = { Text("Product *") },
                        isError = viewModel.showErrors && (viewModel.productId == null || viewModel.productId == 0L),
                        trailingIcon = {
                            Row {
                                if (productSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        productSearchQuery = ""
                                        viewModel.productId = 0L
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductMenu)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showProductMenu,
                        onDismissRequest = { showProductMenu = false }
                    ) {
                        filteredProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    viewModel.productId = product.id
                                    productSearchQuery = product.name
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
                        onValueChange = { viewModel.onQuantityChange(it) },
                        label = { Text("Qty *") },
                        isError = viewModel.showErrors && (viewModel.quantity.isEmpty() || viewModel.quantity == "0"),
                        trailingIcon = {
                            if (viewModel.quantity.isNotEmpty() && viewModel.quantity != "0") {
                                IconButton(onClick = { viewModel.onQuantityChange("0") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (it.isFocused && viewModel.quantity == "0") viewModel.onQuantityChange("")
                        },
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    var unitSearchQuery by remember { mutableStateOf(units.find { it.id == viewModel.unitId }?.symbol ?: "") }
                    LaunchedEffect(viewModel.unitId, units) {
                        unitSearchQuery = units.find { it.id == viewModel.unitId }?.symbol ?: ""
                    }
                    val filteredUnits = remember(units, unitSearchQuery) {
                        if (unitSearchQuery.isBlank()) units
                        else units.filter { it.name.contains(unitSearchQuery, ignoreCase = true) || it.symbol.contains(unitSearchQuery, ignoreCase = true) }
                    }

                    ExposedDropdownMenuBox(
                        expanded = showUnitMenu,
                        onExpandedChange = { showUnitMenu = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unitSearchQuery,
                            onValueChange = { 
                                unitSearchQuery = it
                                showUnitMenu = true
                            },
                            readOnly = false,
                            label = { Text("Unit *") },
                            isError = viewModel.showErrors && (viewModel.unitId == null || viewModel.unitId == 0L),
                            trailingIcon = {
                                Row {
                                    if (unitSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            unitSearchQuery = ""
                                            viewModel.unitId = 0L
                                        }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false }
                        ) {
                            filteredUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.symbol})", style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        viewModel.unitId = unit.id
                                        unitSearchQuery = unit.symbol
                                        showUnitMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.rate,
                        onValueChange = { viewModel.onRateChange(it) },
                        label = { Text("Rate *") },
                        isError = viewModel.showErrors && (viewModel.rate.isEmpty() || viewModel.rate == "0"),
                        trailingIcon = {
                            if (viewModel.rate.isNotEmpty() && viewModel.rate != "0") {
                                IconButton(onClick = { viewModel.onRateChange("0") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (it.isFocused && viewModel.rate == "0") viewModel.onRateChange("")
                        },
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    OutlinedTextField(
                        value = viewModel.amountText,
                        onValueChange = { viewModel.onAmountInputChange(it) },
                        label = { Text("Amount *") },
                        isError = viewModel.showErrors && (viewModel.amountText.isEmpty() || viewModel.amountText == "0"),
                        trailingIcon = {
                            if (viewModel.amountText.isNotEmpty() && viewModel.amountText != "0") {
                                IconButton(onClick = { viewModel.onAmountInputChange("0") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).onFocusChanged {
                            if (it.isFocused && viewModel.amountText == "0") viewModel.onAmountInputChange("")
                        },
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            } else if (!viewModel.isTransferMode) {
                if (viewModel.businessType == BusinessTransactionType.EXPENSE) {
                    // Expense Category Selection
                    var expenseCategorySearchQuery by remember { mutableStateOf(expenseCategories.find { it.id == viewModel.expenseCategoryId }?.name ?: "") }
                    LaunchedEffect(viewModel.expenseCategoryId, expenseCategories) {
                        expenseCategorySearchQuery = expenseCategories.find { it.id == viewModel.expenseCategoryId }?.name ?: ""
                    }
                    val filteredExpenseCategories = remember(expenseCategories, expenseCategorySearchQuery) {
                        if (expenseCategorySearchQuery.isBlank()) expenseCategories
                        else expenseCategories.filter { it.name.contains(expenseCategorySearchQuery, ignoreCase = true) }
                    }

                    ExposedDropdownMenuBox(
                        expanded = showExpenseCategoryMenu,
                        onExpandedChange = { showExpenseCategoryMenu = it }
                    ) {
                        OutlinedTextField(
                            value = expenseCategorySearchQuery,
                            onValueChange = { 
                                expenseCategorySearchQuery = it
                                showExpenseCategoryMenu = true
                            },
                            readOnly = false,
                            label = { Text("Expense Category *") },
                            isError = viewModel.showErrors && (viewModel.expenseCategoryId == null || viewModel.expenseCategoryId == 0L),
                            trailingIcon = {
                                Row {
                                    if (expenseCategorySearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            expenseCategorySearchQuery = ""
                                            viewModel.expenseCategoryId = 0L
                                        }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showExpenseCategoryMenu)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showExpenseCategoryMenu,
                            onDismissRequest = { showExpenseCategoryMenu = false }
                        ) {
                            filteredExpenseCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name, style = MaterialTheme.typography.bodySmall) },
                                    onClick = {
                                        viewModel.expenseCategoryId = category.id
                                        expenseCategorySearchQuery = category.name
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
                        trailingIcon = {
                            if (viewModel.amountText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.amountText = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
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
                        trailingIcon = {
                            if (viewModel.amountText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.amountText = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
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
                        trailingIcon = {
                            if (viewModel.amountText.isNotEmpty()) {
                                IconButton(onClick = { viewModel.amountText = "" }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Financial Account Selection (For Expense Only now, others handled by Unified)
            if (viewModel.businessType == BusinessTransactionType.EXPENSE) {
                var accountSearchQuery by remember { mutableStateOf(financialAccounts.find { it.id == viewModel.financialAccountId }?.name ?: "") }
                LaunchedEffect(viewModel.financialAccountId, financialAccounts) {
                    accountSearchQuery = financialAccounts.find { it.id == viewModel.financialAccountId }?.name ?: ""
                }
                val filteredAccounts = remember(financialAccounts, accountSearchQuery) {
                    if (accountSearchQuery.isBlank()) financialAccounts
                    else financialAccounts.filter { it.name.contains(accountSearchQuery, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = showAccountMenu,
                        onExpandedChange = { showAccountMenu = it }
                    ) {
                        OutlinedTextField(
                            value = accountSearchQuery,
                            onValueChange = { 
                                accountSearchQuery = it
                                showAccountMenu = true
                            },
                            readOnly = false,
                            label = { Text("Payment Method (Account) *") },
                            isError = viewModel.showErrors && (viewModel.financialAccountId == null || viewModel.financialAccountId == 0L),
                            trailingIcon = {
                                Row {
                                    if (accountSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { 
                                            accountSearchQuery = ""
                                            viewModel.financialAccountId = 0L
                                        }) {
                                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountMenu)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        ExposedDropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false }
                        ) {
                            filteredAccounts.forEach { account ->
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
                                        accountSearchQuery = account.name
                                        showAccountMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Additional Costs Section
            if (viewModel.businessType == BusinessTransactionType.PURCHASE || 
                viewModel.businessType == BusinessTransactionType.SALE) {
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Additional Costs", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { viewModel.addCostRow() }) {
                        Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Cost")
                    }
                }

                viewModel.additionalCosts.forEachIndexed { index, cost ->
                    CostRowItem(
                        cost = cost,
                        unifiedAccounts = viewModel.unifiedAccounts.collectAsState().value,
                        onCostChange = { updated -> viewModel.updateCostRow(index, updated) },
                        onRemove = { viewModel.removeCostRow(index) }
                    )
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
                        Text("Total Amount", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            viewModel.totalAmount.toPlainString(),
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
                trailingIcon = {
                    if (viewModel.note.isNotEmpty()) {
                        IconButton(onClick = { viewModel.note = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostRowItem(
    cost: CostRow,
    unifiedAccounts: List<UnifiedAccount>,
    onCostChange: (CostRow) -> Unit,
    onRemove: () -> Unit
) {
    var showTypeMenu by remember { mutableStateOf(false) }
    var showSrcMenu by remember { mutableStateOf(false) }
    var showDestMenu by remember { mutableStateOf(false) }

    var srcSearchQuery by remember { mutableStateOf(cost.sourceAccount?.name ?: "") }
    var destSearchQuery by remember { mutableStateOf(cost.destinationAccount?.name ?: "") }

    LaunchedEffect(cost.sourceAccount) {
        srcSearchQuery = cost.sourceAccount?.name ?: ""
    }
    LaunchedEffect(cost.destinationAccount) {
        destSearchQuery = cost.destinationAccount?.name ?: ""
    }

    val filteredSrcAccounts = remember(unifiedAccounts, srcSearchQuery) {
        if (srcSearchQuery.isBlank()) unifiedAccounts
        else unifiedAccounts.filter { it.name.contains(srcSearchQuery, ignoreCase = true) }
    }

    val filteredDestAccounts = remember(unifiedAccounts, destSearchQuery) {
        if (destSearchQuery.isBlank()) unifiedAccounts
        else unifiedAccounts.filter { it.name.contains(destSearchQuery, ignoreCase = true) }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cost Type Selector
                ExposedDropdownMenuBox(
                    expanded = showTypeMenu,
                    onExpandedChange = { showTypeMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = cost.type.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cost Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeMenu) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        LinkedTransactionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    onCostChange(cost.copy(type = type))
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = cost.amount,
                    onValueChange = { onCostChange(cost.copy(amount = it)) },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Source Account
                ExposedDropdownMenuBox(
                    expanded = showSrcMenu,
                    onExpandedChange = { showSrcMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = srcSearchQuery,
                        onValueChange = {
                            srcSearchQuery = it
                            showSrcMenu = true
                        },
                        readOnly = false,
                        label = { Text("Payer (From)") },
                        trailingIcon = {
                            Row {
                                if (srcSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        srcSearchQuery = ""
                                        onCostChange(cost.copy(sourceAccount = null))
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSrcMenu)
                            }
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showSrcMenu,
                        onDismissRequest = { showSrcMenu = false }
                    ) {
                        filteredSrcAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(account.name, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            if (account is UnifiedAccount.Financial) "Acc" else "Party",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                onClick = {
                                    onCostChange(cost.copy(sourceAccount = account))
                                    srcSearchQuery = account.name
                                    showSrcMenu = false
                                }
                            )
                        }
                    }
                }

                // Destination Account
                ExposedDropdownMenuBox(
                    expanded = showDestMenu,
                    onExpandedChange = { showDestMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = destSearchQuery,
                        onValueChange = {
                            destSearchQuery = it
                            showDestMenu = true
                        },
                        readOnly = false,
                        label = { Text("Payee (To)") },
                        trailingIcon = {
                            Row {
                                if (destSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        destSearchQuery = ""
                                        onCostChange(cost.copy(destinationAccount = null))
                                    }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDestMenu)
                            }
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = showDestMenu,
                        onDismissRequest = { showDestMenu = false }
                    ) {
                        filteredDestAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { 
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(account.name, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            if (account is UnifiedAccount.Financial) "Acc" else "Party",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                onClick = {
                                    onCostChange(cost.copy(destinationAccount = account))
                                    destSearchQuery = account.name
                                    showDestMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
