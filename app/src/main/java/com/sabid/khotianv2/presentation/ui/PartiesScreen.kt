package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.CommaStyle
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.presentation.viewmodel.DashboardViewModel
import com.sabid.khotianv2.util.CurrencyUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartiesScreen(
    viewModel: DashboardViewModel,
    onPartyClick: (Long) -> Unit,
    onAddPartyClick: () -> Unit
) {
    val parties by viewModel.parties.collectAsState()
    val commaStyle by viewModel.commaStyle.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredParties = remember(parties, searchQuery) {
        if (searchQuery.isBlank()) parties
        else parties.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPartyClick,
                modifier = Modifier.imePadding()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Party")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search parties...") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )
            
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredParties) { party ->
                    PartyCard(
                        party = party,
                        commaStyle = commaStyle,
                        onClick = { onPartyClick(party.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PartyCard(
    party: Party,
    commaStyle: CommaStyle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = party.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = party.type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "৳ ${CurrencyUtils.formatAmount(party.currentBalance, commaStyle)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (party.currentBalance >= BigDecimal.ZERO) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
                Text(
                    text = if (party.currentBalance >= BigDecimal.ZERO) "Debit" else "Credit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
