package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.DailyEntryEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: SalonSettingsEntity,
    selectedDate: String,
    activeEmployees: List<EmployeeEntity>,
    todayEntries: List<DailyEntryEntity>,
    todayExpenses: List<ExpenseEntity>,
    voiceFeedback: String?,
    onDateOffset: (Int) -> Unit,
    onSaveEmployeeEntry: (employeeId: Long, employeeName: String, cash: Double, online: Double, breakdown: String) -> Unit,
    onAddExpense: (name: String, category: String, amount: Double, notes: String) -> Unit,
    onProcessVoice: (String) -> Unit,
    onClearVoiceFeedback: () -> Unit
) {
    val context = LocalContext.current
    var voiceInputText by remember { mutableStateOf("") }
    var showExpenseDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultsList: java.util.ArrayList<String>? = result.data
                ?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText: String? = resultsList?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                voiceInputText = spokenText
                onProcessVoice(spokenText)
            }
        }
    }

    val totalCash = todayEntries.sumOf { it.cashEarnings }
    val totalOnline = todayEntries.sumOf { it.onlinePayments }
    val totalRevenue = totalCash + totalOnline
    val totalExpenseAmount = todayExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenseAmount

    val currencySym = settings.currencySymbol

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = settings.salonName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${settings.ownerName} • ${settings.workingHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Add Expense Action Button
                    Button(
                        onClick = { showExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("top_add_expense_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.get("add_expense", settings.language),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { onDateOffset(-1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                            }
                            Text(
                                text = selectedDate,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = { onDateOffset(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Summary Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Total Revenue Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            com.example.ui.theme.ImmersiveBlueDark,
                                            com.example.ui.theme.ImmersiveBlue
                                        )
                                    )
                                )
                                .padding(22.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = com.example.ui.theme.ImmersiveEmerald,
                                            modifier = Modifier.size(8.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Strings.get("today_revenue", settings.language).uppercase(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = selectedDate,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "$currencySym ${totalRevenue.toInt()}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 36.sp
                                    ),
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${Strings.get("cash_revenue", settings.language)}: $currencySym ${totalCash.toInt()}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = com.example.ui.theme.ImmersiveEmeraldLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${Strings.get("online_revenue", settings.language)}: $currencySym ${totalOnline.toInt()}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expenses & Profit Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.get("today_expenses", settings.language),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    IconButton(
                                        onClick = { showExpenseDialog = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = Strings.get("add_expense", settings.language),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$currencySym ${totalExpenseAmount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = Strings.get("today_profit", settings.language),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$currencySym ${netProfit.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Voice Command Input Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(
                                            android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                                            if (settings.language == "UR") "ur-PK" else if (settings.language == "AR") "ar-SA" else "en-US"
                                        )
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak command (e.g., 'Ahmed 500 cash' or 'Chai 100')")
                                    }
                                    try {
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Voice recognition not available on this device", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .size(40.dp)
                                    .testTag("mic_voice_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            OutlinedTextField(
                                value = voiceInputText,
                                onValueChange = { voiceInputText = it },
                                placeholder = {
                                    Text(
                                        text = Strings.get("voice_hint", settings.language),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("voice_input_field"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (voiceInputText.isNotBlank()) {
                                        onProcessVoice(voiceInputText)
                                        voiceInputText = ""
                                        focusManager.clearFocus()
                                    }
                                }),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (voiceInputText.isNotBlank()) {
                                        onProcessVoice(voiceInputText)
                                        voiceInputText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Submit Voice Command",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (voiceFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = voiceFeedback,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(
                                    onClick = onClearVoiceFeedback,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Quick Daily Entry Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("quick_entry", settings.language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${activeEmployees.size} ${Strings.get("nav_employees", settings.language)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Employee Cards
            items(activeEmployees, key = { it.id }) { emp ->
                val entry = todayEntries.find { it.employeeId == emp.id }
                EmployeeDataCard(
                    employee = emp,
                    entry = entry,
                    currencySymbol = currencySym,
                    lang = settings.language,
                    onSave = { cash, online, breakdown ->
                        onSaveEmployeeEntry(emp.id, emp.name, cash, online, breakdown)
                    }
                )
            }
        }
    }

    if (showExpenseDialog) {
        AddExpenseDialog(
            lang = settings.language,
            currencySymbol = currencySym,
            onDismiss = { showExpenseDialog = false },
            onConfirm = { name, category, amount, notes ->
                onAddExpense(name, category, amount, notes)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
fun EmployeeDataCard(
    employee: EmployeeEntity,
    entry: DailyEntryEntity?,
    currencySymbol: String,
    lang: String,
    onSave: (cash: Double, online: Double, breakdown: String) -> Unit
) {
    var cashText by remember(entry?.cashEarnings) {
        mutableStateOf(if ((entry?.cashEarnings ?: 0.0) > 0) entry!!.cashEarnings.toInt().toString() else "")
    }
    var onlineText by remember(entry?.onlinePayments) {
        mutableStateOf(if ((entry?.onlinePayments ?: 0.0) > 0) entry!!.onlinePayments.toInt().toString() else "")
    }

    val initialAmounts = remember(entry?.amountBreakdown) {
        entry?.amountBreakdown?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
    val addedAmounts = remember { mutableStateListOf<String>().apply { addAll(initialAmounts) } }

    var newAmountText by remember { mutableStateOf("") }
    var calculatedTotalFeedback by remember { mutableStateOf<Double?>(null) }

    val currentSum = addedAmounts.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val cashVal = cashText.toDoubleOrNull() ?: 0.0
    val onlineVal = onlineText.toDoubleOrNull() ?: 0.0
    val displayTotal = if (currentSum > 0) currentSum else (cashVal + onlineVal)

    fun syncAndSave(newCash: Double, newOnline: Double, amountsList: List<String>) {
        val bd = amountsList.joinToString(",")
        onSave(newCash, newOnline, bd)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("emp_card_${employee.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Avatar, Name, Role & Total Income Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = employee.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = employee.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "$currencySymbol ${displayTotal.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Amount Entry Label
            Text(
                text = Strings.get("enter_amount", lang),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newAmountText,
                    onValueChange = { newAmountText = it.filter { c -> c.isDigit() } },
                    placeholder = { Text("200, 400, 700...") },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("multi_amount_input_${employee.id}"),
                    shape = RoundedCornerShape(16.dp)
                )

                Button(
                    onClick = {
                        val amt = newAmountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            addedAmounts.add(amt.toInt().toString())
                            newAmountText = ""
                            val totalSum = addedAmounts.sumOf { it.toDoubleOrNull() ?: 0.0 }
                            cashText = totalSum.toInt().toString()
                            syncAndSave(totalSum, onlineVal, addedAmounts)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("add_amount_btn_${employee.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("add_amount", lang), style = MaterialTheme.typography.labelMedium)
                }
            }

            // Quick Preset Amount Chips (+200, +400, +500, +700, +1000)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(200, 400, 500, 700, 1000).forEach { preset ->
                    SuggestionChip(
                        onClick = {
                            addedAmounts.add(preset.toString())
                            val totalSum = addedAmounts.sumOf { it.toDoubleOrNull() ?: 0.0 }
                            cashText = totalSum.toInt().toString()
                            syncAndSave(totalSum, onlineVal, addedAmounts)
                        },
                        label = { Text("+$preset", style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Display Added Amounts List (e.g. 200, 400, 700)
            if (addedAmounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Strings.get("added_amounts", lang)} (${addedAmounts.size}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                addedAmounts.clear()
                                cashText = ""
                                calculatedTotalFeedback = null
                                syncAndSave(0.0, onlineVal, emptyList())
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(Strings.get("clear_all", lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        addedAmounts.forEachIndexed { index, amountStr ->
                            InputChip(
                                selected = false,
                                onClick = {
                                    addedAmounts.removeAt(index)
                                    val totalSum = addedAmounts.sumOf { it.toDoubleOrNull() ?: 0.0 }
                                    cashText = if (totalSum > 0) totalSum.toInt().toString() else ""
                                    syncAndSave(totalSum, onlineVal, addedAmounts)
                                },
                                label = { Text("$currencySymbol $amountStr") },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // CALCULATE TOTAL INCOME BUTTON
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    val totalSum = if (addedAmounts.isNotEmpty()) {
                        addedAmounts.sumOf { it.toDoubleOrNull() ?: 0.0 }
                    } else {
                        cashVal + onlineVal
                    }
                    if (addedAmounts.isNotEmpty()) {
                        cashText = totalSum.toInt().toString()
                    }
                    calculatedTotalFeedback = totalSum
                    syncAndSave(cashText.toDoubleOrNull() ?: 0.0, onlineVal, addedAmounts)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_total_btn_${employee.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("calculate_total", lang),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Calculated Feedback Banner
            if (calculatedTotalFeedback != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${employee.name}: $currencySymbol ${calculatedTotalFeedback!!.toInt()}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = Strings.get("total_calculated", lang),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Direct Cash & Online Fields
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cashText,
                    onValueChange = {
                        cashText = it.filter { c -> c.isDigit() }
                        val c = cashText.toDoubleOrNull() ?: 0.0
                        val o = onlineText.toDoubleOrNull() ?: 0.0
                        syncAndSave(c, o, addedAmounts)
                    },
                    label = { Text(Strings.get("cash_earnings", lang)) },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("cash_input_${employee.id}"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = onlineText,
                    onValueChange = {
                        onlineText = it.filter { c -> c.isDigit() }
                        val c = cashText.toDoubleOrNull() ?: 0.0
                        val o = onlineText.toDoubleOrNull() ?: 0.0
                        syncAndSave(c, o, addedAmounts)
                    },
                    label = { Text(Strings.get("online_payments", lang)) },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("online_input_${employee.id}"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    lang: String,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, amount: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tea") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        "Tea", "Coffee", "Food", "Drinks", "Electricity",
        "Cleaning Supplies", "Hair Products", "Equipment Repair", "Miscellaneous"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.get("add_expense", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.get("expense_name", lang)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text(Strings.get("expense_amount", lang)) },
                    leadingIcon = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Column {
                    Text(
                        text = Strings.get("expense_category", lang),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(categories) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = category == cat, onClick = { category = cat })
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.get("cat_" + cat.lowercase().take(4), lang).ifEmpty { cat },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(Strings.get("expense_notes", lang)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onConfirm(name, category, amt, notes)
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
