package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.presentation.viewmodel.CrushingEntryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrushingEntryScreen(
    viewModel: CrushingEntryViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Crushing Entry", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = viewModel.batchNumber,
                    onValueChange = { viewModel.batchNumber = it },
                    label = { Text("Batch #") },
                    trailingIcon = {
                        if (viewModel.batchNumber.isNotEmpty()) {
                            IconButton(onClick = { viewModel.batchNumber = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.seedType,
                    onValueChange = { viewModel.seedType = it },
                    label = { Text("Seed Type") },
                    trailingIcon = {
                        if (viewModel.seedType.isNotEmpty()) {
                            IconButton(onClick = { viewModel.seedType = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = viewModel.seedQuantity,
                    onValueChange = { viewModel.seedQuantity = it },
                    label = { Text("Seed Qty (kg)") },
                    trailingIcon = {
                        if (viewModel.seedQuantity.isNotEmpty()) {
                            IconButton(onClick = { viewModel.seedQuantity = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.seedRate,
                    onValueChange = { viewModel.seedRate = it },
                    label = { Text("Seed Rate") },
                    trailingIcon = {
                        if (viewModel.seedRate.isNotEmpty()) {
                            IconButton(onClick = { viewModel.seedRate = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = viewModel.oilQuantity,
                    onValueChange = { viewModel.oilQuantity = it },
                    label = { Text("Oil Out") },
                    trailingIcon = {
                        if (viewModel.oilQuantity.isNotEmpty()) {
                            IconButton(onClick = { viewModel.oilQuantity = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.oilCakeQuantity,
                    onValueChange = { viewModel.oilCakeQuantity = it },
                    label = { Text("Cake Out") },
                    trailingIcon = {
                        if (viewModel.oilCakeQuantity.isNotEmpty()) {
                            IconButton(onClick = { viewModel.oilCakeQuantity = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = viewModel.wasteQuantity,
                    onValueChange = { viewModel.wasteQuantity = it },
                    label = { Text("Waste") },
                    trailingIcon = {
                        if (viewModel.wasteQuantity.isNotEmpty()) {
                            IconButton(onClick = { viewModel.wasteQuantity = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = viewModel.crushingCharge,
                    onValueChange = { viewModel.crushingCharge = it },
                    label = { Text("Crushing Charge") },
                    trailingIcon = {
                        if (viewModel.crushingCharge.isNotEmpty()) {
                            IconButton(onClick = { viewModel.crushingCharge = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = viewModel.note,
                onValueChange = { viewModel.note = it },
                label = { Text("Note") },
                trailingIcon = {
                    if (viewModel.note.isNotEmpty()) {
                        IconButton(onClick = { viewModel.note = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.submit(
                        onSuccess = onSuccess,
                        onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isSubmitting
            ) {
                if (viewModel.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Batch")
                }
            }
        }
    }
}
