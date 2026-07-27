package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.EmployeeEntity
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    settings: SalonSettingsEntity,
    allEmployees: List<EmployeeEntity>,
    onAddEmployee: (name: String, role: String, phone: String) -> Unit,
    onUpdateEmployee: (EmployeeEntity) -> Unit,
    onDeleteEmployee: (EmployeeEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var deletingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    val lang = settings.language

    val filteredEmployees = remember(allEmployees, searchQuery) {
        if (searchQuery.isBlank()) allEmployees
        else allEmployees.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.role.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("nav_employees", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_emp_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(Strings.get("search", lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("emp_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            if (filteredEmployees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.get("no_data", lang),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEmployees, key = { it.id }) { emp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("employee_item_${emp.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (emp.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = emp.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (emp.isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = emp.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (emp.isEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    text = if (emp.isEnabled) Strings.get("enabled", lang) else Strings.get("disabled", lang),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (emp.isEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = emp.role,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (emp.phone.isNotBlank()) {
                                            Text(
                                                text = emp.phone,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onUpdateEmployee(emp.copy(isEnabled = !emp.isEnabled))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (emp.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Status",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(onClick = { editingEmployee = emp }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    IconButton(onClick = { deletingEmployee = emp }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EmployeeDialog(
            lang = lang,
            title = Strings.get("add_employee", lang),
            initialName = "",
            initialRole = "Barber",
            initialPhone = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, role, phone ->
                onAddEmployee(name, role, phone)
                showAddDialog = false
            }
        )
    }

    editingEmployee?.let { emp ->
        EmployeeDialog(
            lang = lang,
            title = Strings.get("edit_employee", lang),
            initialName = emp.name,
            initialRole = emp.role,
            initialPhone = emp.phone,
            onDismiss = { editingEmployee = null },
            onConfirm = { name, role, phone ->
                onUpdateEmployee(emp.copy(name = name, role = role, phone = phone))
                editingEmployee = null
            }
        )
    }

    deletingEmployee?.let { emp ->
        AlertDialog(
            onDismissRequest = { deletingEmployee = null },
            title = { Text(Strings.get("remove_employee", lang)) },
            text = { Text("Are you sure you want to delete ${emp.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEmployee(emp)
                        deletingEmployee = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(Strings.get("delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEmployee = null }) {
                    Text(Strings.get("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun EmployeeDialog(
    lang: String,
    title: String,
    initialName: String,
    initialRole: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var role by remember { mutableStateOf(initialRole) }
    var phone by remember { mutableStateOf(initialPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("employee_name", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text(Strings.get("employee_role", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Strings.get("employee_phone", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, role, phone)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Strings.get("save", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.get("cancel", lang))
            }
        }
    )
}
