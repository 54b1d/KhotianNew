package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sabid.khotianv2.presentation.viewmodel.ProductEntryState
import com.sabid.khotianv2.presentation.viewmodel.ProductEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductEntryScreen(
    viewModel: ProductEntryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedUnitId by remember { mutableLongStateOf(0L) }
    var category by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    val units by viewModel.units.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var unitDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ProductEntryState.Success) {
            name = ""
            category = ""
            selectedUnitId = 0L
            openingBalance = ""
            snackbarHostState.showSnackbar("Product added successfully")
            viewModel.resetState()
        } else if (uiState is ProductEntryState.Error) {
            snackbarHostState.showSnackbar((uiState as ProductEntryState.Error).message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                trailingIcon = {
                    if (name.isNotEmpty()) {
                        IconButton(onClick = { name = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = unitDropdownExpanded,
                onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = units.find { it.id == selectedUnitId }?.name ?: "Select Unit",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Default Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unitDropdownExpanded,
                    onDismissRequest = { unitDropdownExpanded = false }
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name) },
                            onClick = {
                                selectedUnitId = unit.id
                                unitDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (Optional)") },
                trailingIcon = {
                    if (category.isNotEmpty()) {
                        IconButton(onClick = { category = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = openingBalance,
                onValueChange = { openingBalance = it },
                label = { Text("Opening Balance (Optional)") },
                trailingIcon = {
                    if (openingBalance.isNotEmpty()) {
                        IconButton(onClick = { openingBalance = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )

            Button(
                onClick = {
                    viewModel.addProduct(
                        name,
                        if (selectedUnitId == 0L) null else selectedUnitId,
                        if (category.isBlank()) null else category,
                        openingBalance
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is ProductEntryState.Loading
            ) {
                if (uiState is ProductEntryState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Product")
                }
            }
        }
    }
}
