package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sabid.khotianv2.domain.model.FinancialAccount
import com.sabid.khotianv2.domain.model.FinancialAccountType
import com.sabid.khotianv2.presentation.viewmodel.FinancialAccountEntryViewModel
import com.sabid.khotianv2.presentation.viewmodel.FinancialAccountUiState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAccountEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancialAccountEntryViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FinancialAccountType.CASH) }
    var openingBalance by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState(initial = emptyList())

    LaunchedEffect(uiState) {
        if (uiState is FinancialAccountUiState.Success) {
            showAddDialog = false
            name = ""
            openingBalance = ""
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Accounts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState is FinancialAccountUiState.Error) {
                Text(
                    text = (uiState as FinancialAccountUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    AccountItem(account)
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Financial Account") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Account Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Account Type", style = MaterialTheme.typography.labelLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedType == FinancialAccountType.CASH,
                                onClick = { selectedType = FinancialAccountType.CASH }
                            )
                            Text("Cash")
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = selectedType == FinancialAccountType.BANK,
                                onClick = { selectedType = FinancialAccountType.BANK }
                            )
                            Text("Bank")
                        }

                        OutlinedTextField(
                            value = openingBalance,
                            onValueChange = { openingBalance = it },
                            label = { Text("Opening Balance") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addAccount(
                                name,
                                selectedType,
                                openingBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO
                            )
                        },
                        enabled = name.isNotBlank() && uiState !is FinancialAccountUiState.Loading
                    ) {
                        if (uiState is FinancialAccountUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("Add")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AccountItem(account: FinancialAccount) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(account.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    account.type.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Opening", style = MaterialTheme.typography.labelSmall)
                    Text(account.openingBalance.toString())
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Balance", style = MaterialTheme.typography.labelSmall)
                    Text(
                        account.currentBalance.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
