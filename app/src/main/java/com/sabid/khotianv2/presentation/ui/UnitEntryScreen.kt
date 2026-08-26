package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.presentation.viewmodel.UnitEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitEntryScreen(
    viewModel: UnitEntryViewModel,
    onBack: () -> Unit
) {
    val units by viewModel.units.collectAsState()
    val name by viewModel.name.collectAsState()
    val symbol by viewModel.symbol.collectAsState()
    val multiplier by viewModel.multiplier.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Unit Name (e.g., Kilogram)") },
                    trailingIcon = {
                        if (name.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onNameChange("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = viewModel::onSymbolChange,
                        label = { Text("Symbol (e.g., kg)") },
                        trailingIcon = {
                            if (symbol.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSymbolChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = multiplier,
                        onValueChange = viewModel::onMultiplierChange,
                        label = { Text("Multiplier") },
                        trailingIcon = {
                            if (multiplier.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onMultiplierChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = viewModel::addUnit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && name.isNotBlank() && symbol.isNotBlank() && multiplier.toBigDecimalOrNull() != null
                ) {
                    Text("Add Unit")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Existing Units", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(units) { unit ->
                ListItem(
                    headlineContent = { Text(unit.name) },
                    supportingContent = { Text("Symbol: ${unit.symbol}, Multiplier: ${unit.multiplier}") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteUnit(unit) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
