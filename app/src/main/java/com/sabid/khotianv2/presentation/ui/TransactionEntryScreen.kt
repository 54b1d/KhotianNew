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
    val permissions by viewModel.userPermissions.collectAsState()
    var showPartyMenu by remember { mutableStateOf(false) }
    var showProductMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Transaction", style = MaterialTheme.typography.titleMedium) },
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
                text = { Text("Save") },
                expanded = !viewModel.isSubmitting
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Transaction Type Selection
            Text("Transaction Type", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BusinessTransactionType.entries.forEach { type ->
                    FilterChip(
                        selected = viewModel.businessType == type,
                        onClick = { viewModel.businessType = type },
                        label = { 
                            Text(
                                text = type.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Party Selection
            ExposedDropdownMenuBox(
                expanded = showPartyMenu,
                onExpandedChange = { showPartyMenu = it }
            ) {
                OutlinedTextField(
                    value = parties.find { it.id == viewModel.partyId }?.name ?: "Select Party",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Party") },
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

            // Product Selection (only for Purchase/Sale)
            if (viewModel.businessType == BusinessTransactionType.PURCHASE || 
                viewModel.businessType == BusinessTransactionType.SALE) {
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
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = viewModel.rate,
                        onValueChange = { viewModel.rate = it },
                        label = { Text("Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
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
