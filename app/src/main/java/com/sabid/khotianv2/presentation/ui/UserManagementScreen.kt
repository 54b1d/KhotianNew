package com.sabid.khotianv2.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sabid.khotianv2.domain.model.PermissionType
import com.sabid.khotianv2.domain.model.Role
import com.sabid.khotianv2.domain.model.User
import com.sabid.khotianv2.presentation.viewmodel.UserManagementUiState
import com.sabid.khotianv2.presentation.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel,
    onBackClick: () -> Unit
) {
    val hasAccess by viewModel.hasAccess.collectAsState()
    
    if (!hasAccess) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Access Denied: You don't have permission to manage users.")
        }
        return
    }

    val users by viewModel.users.collectAsState()
    val roles by viewModel.roles.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddRoleDialog by remember { mutableStateOf(false) }
    var editingRole by remember { mutableStateOf<Role?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is UserManagementUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearState()
            }
            is UserManagementUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User & Role Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddUserDialog = true
                    else showAddRoleDialog = true
                }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Users") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Roles") }
                )
            }

            if (selectedTab == 0) {
                UserList(
                    users = users,
                    onDeleteUser = { viewModel.deleteUser(it.id) }
                )
            } else {
                RoleList(
                    roles = roles,
                    onEditRole = {
                        editingRole = it
                        showAddRoleDialog = true
                    },
                    onDeleteRole = { viewModel.deleteRole(it.id) }
                )
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            roles = roles,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, pin, roleId ->
                viewModel.createUser(username, pin, roleId)
                showAddUserDialog = false
            }
        )
    }

    if (showAddRoleDialog) {
        AddRoleDialog(
            role = editingRole,
            onDismiss = { 
                showAddRoleDialog = false
                editingRole = null
            },
            onConfirm = { name, permissions ->
                viewModel.upsertRole(editingRole?.id ?: 0, name, permissions)
                showAddRoleDialog = false
                editingRole = null
            }
        )
    }
}

@Composable
fun UserList(
    users: List<User>,
    onDeleteUser: (User) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users) { user ->
            ListItem(
                headlineContent = { Text(user.username) },
                supportingContent = { Text("Role: ${user.roleName ?: "None"}") },
                trailingContent = {
                    IconButton(onClick = { onDeleteUser(user) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun RoleList(
    roles: List<Role>,
    onEditRole: (Role) -> Unit,
    onDeleteRole: (Role) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(roles) { role ->
            ListItem(
                headlineContent = { Text(role.name) },
                supportingContent = { 
                    Text(
                        role.permissions.joinToString { it.name.lowercase().replace("can_", "").replace("_", " ") },
                        maxLines = 1
                    ) 
                },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEditRole(role) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDeleteRole(role) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun AddUserDialog(
    roles: List<Role>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedRoleId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = roles.find { it.id == selectedRoleId }?.name ?: "Select Role",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = "Expand") // Should be arrow drop down
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRoleId = role.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username, pin, selectedRoleId) },
                enabled = username.isNotBlank() && pin.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRoleDialog(
    role: Role?,
    onDismiss: () -> Unit,
    onConfirm: (String, List<PermissionType>) -> Unit
) {
    var name by remember { mutableStateOf(role?.name ?: "") }
    var selectedPermissions by remember { mutableStateOf(role?.permissions?.toSet() ?: emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (role == null) "Add Role" else "Edit Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Role Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Permissions", style = MaterialTheme.typography.titleSmall)
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PermissionType.values().forEach { permission ->
                        FilterChip(
                            selected = selectedPermissions.contains(permission),
                            onClick = {
                                selectedPermissions = if (selectedPermissions.contains(permission)) {
                                    selectedPermissions - permission
                                } else {
                                    selectedPermissions + permission
                                }
                            },
                            label = { Text(permission.name.lowercase().replace("can_", "").replace("_", " ")) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedPermissions.toList()) },
                enabled = name.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
