package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.Party
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onPartyClick: (Long) -> Unit,
    onAddPartyClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onCrushingEntryClick: () -> Unit
) {
    val parties by viewModel.parties.collectAsState()
    val permissions by viewModel.userPermissions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.titleMedium) }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (permissions.hasPermission(PermissionType.CAN_MANAGE_FACTORY)) {
                    ExtendedFloatingActionButton(
                        onClick = onCrushingEntryClick,
                        icon = { Icon(Icons.Rounded.Factory, null) },
                        text = { Text("Crushing") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                if (permissions.hasPermission(PermissionType.CAN_EDIT_TRANSACTIONS)) {
                    SmallFloatingActionButton(
                        onClick = onAddPartyClick,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Party")
                    }
                    FloatingActionButton(onClick = onAddTransactionClick) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
                    }
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(parties) { party ->
                PartyCard(party = party, onClick = { onPartyClick(party.id) })
            }
        }
    }
}

@Composable
private fun PartyCard(party: Party, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = party.name,
                    style = MaterialTheme.typography.bodyMedium,
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
    }
}
