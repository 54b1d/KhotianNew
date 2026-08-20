package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
    var unit by remember { mutableStateOf("Kg") } // Default unit
    var category by remember { mutableStateOf("") }
    
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is ProductEntryState.Success) {
            name = ""
            category = ""
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (e.g., Kg, Liter, Bag)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.addProduct(name, unit, if (category.isBlank()) null else category) },
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
