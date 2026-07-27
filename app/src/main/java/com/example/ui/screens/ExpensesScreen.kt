package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.ExpenseEntity
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    settings: SalonSettingsEntity,
    allExpenses: List<ExpenseEntity>,
    onAddExpense: (name: String, category: String, amount: Double, notes: String) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val lang = settings.language
    val currencySym = settings.currencySymbol

    val categories = listOf("All", "Tea", "Coffee", "Food", "Drinks", "Electricity", "Cleaning Supplies", "Hair Products", "Equipment Repair", "Miscellaneous")

    val filteredExpenses = remember(allExpenses, searchQuery, selectedCategory) {
        allExpenses.filter { exp ->
            val matchesCategory = selectedCategory == "All" || exp.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    exp.name.contains(searchQuery, ignoreCase = true) ||
                    exp.category.contains(searchQuery, ignoreCase = true) ||
                    exp.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val totalFilteredAmount = filteredExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("expenses", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("expense_page_add_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.get("add_expense", lang),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Filter Categories Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                if (cat == "All") Strings.get("all", lang)
                                else Strings.get("cat_" + cat.lowercase().take(4), lang).ifEmpty { cat }
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Search Bar
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
                    .padding(bottom = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Total Expenses Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Strings.get("today_expenses", lang)} (${filteredExpenses.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "$currencySym ${totalFilteredAmount.toInt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (exp.category.lowercase()) {
                                                    "tea", "coffee" -> Icons.Default.LocalCafe
                                                    "food", "drinks" -> Icons.Default.Restaurant
                                                    "electricity" -> Icons.Default.Bolt
                                                    "cleaning supplies" -> Icons.Default.CleaningServices
                                                    "hair products" -> Icons.Default.ContentCut
                                                    else -> Icons.Default.Receipt
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = exp.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${exp.category} • ${exp.date}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (exp.notes.isNotBlank()) {
                                            Text(
                                                text = exp.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$currencySym ${exp.amount.toInt()}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    IconButton(onClick = { onDeleteExpense(exp) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Expense",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
        AddExpenseDialog(
            lang = lang,
            currencySymbol = currencySym,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, amount, notes ->
                onAddExpense(name, category, amount, notes)
                showAddDialog = false
            }
        )
    }
}
