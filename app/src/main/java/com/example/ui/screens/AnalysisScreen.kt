package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DailyEntryEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    settings: SalonSettingsEntity,
    allEmployees: List<EmployeeEntity>,
    allDailyEntries: List<DailyEntryEntity>,
    allExpenses: List<ExpenseEntity>
) {
    val currencySym = settings.currencySymbol
    val lang = settings.language

    val totalCash = allDailyEntries.sumOf { it.cashEarnings }
    val totalOnline = allDailyEntries.sumOf { it.onlinePayments }
    val totalRevenue = totalCash + totalOnline
    val totalExpensesAmount = allExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpensesAmount

    val hasData = allDailyEntries.isNotEmpty() || allExpenses.isNotEmpty()

    // Employee performance map
    val empEarnings = remember(allDailyEntries) {
        allDailyEntries.groupBy { it.employeeName }
            .mapValues { entry -> entry.value.sumOf { it.cashEarnings + it.onlinePayments } }
    }

    val highestEarner = empEarnings.maxByOrNull { it.value }
    val lowestEarner = empEarnings.filter { it.value > 0 }.minByOrNull { it.value }
    val avgEarnings = if (allEmployees.isNotEmpty()) totalRevenue / allEmployees.size else 0.0

    // Expense breakdown by category
    val expenseCategories = remember(allExpenses) {
        allExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("analytics_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (!hasData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Strings.get("no_data", lang),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.get("no_data_subtitle", lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // High-level Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = Strings.get("highest_earner", lang),
                                value = highestEarner?.let { "${it.key} ($currencySym${it.value.toInt()})" } ?: "-",
                                icon = Icons.Default.Star,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = Strings.get("lowest_earner", lang),
                                value = lowestEarner?.let { "${it.key} ($currencySym${it.value.toInt()})" } ?: "-",
                                icon = Icons.Default.TrendingDown,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = Strings.get("avg_earnings", lang),
                                value = "$currencySym ${avgEarnings.toInt()}",
                                icon = Icons.Default.Group,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = Strings.get("today_profit", lang),
                                value = "$currencySym ${netProfit.toInt()}",
                                icon = Icons.Default.AccountBalanceWallet,
                                color = if (netProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Payment Method Donut Chart (Cash vs Online)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = Strings.get("payment_split", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val primaryColor = MaterialTheme.colorScheme.primary
                            val secondaryColor = MaterialTheme.colorScheme.secondary

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(130.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        val total = totalCash + totalOnline
                                        val cashAngle = if (total > 0) (totalCash / total * 360f).toFloat() else 180f
                                        val onlineAngle = if (total > 0) (totalOnline / total * 360f).toFloat() else 180f

                                        drawArc(
                                            color = primaryColor,
                                            startAngle = -90f,
                                            sweepAngle = cashAngle,
                                            useCenter = false,
                                            style = Stroke(width = 30f)
                                        )

                                        drawArc(
                                            color = secondaryColor,
                                            startAngle = -90f + cashAngle,
                                            sweepAngle = onlineAngle,
                                            useCenter = false,
                                            style = Stroke(width = 30f)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$currencySym ${totalRevenue.toInt()}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = Strings.get("total_revenue", lang),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LegendItem(
                                        color = primaryColor,
                                        label = Strings.get("cash_revenue", lang),
                                        amount = "$currencySym ${totalCash.toInt()}"
                                    )
                                    LegendItem(
                                        color = secondaryColor,
                                        label = Strings.get("online_revenue", lang),
                                        amount = "$currencySym ${totalOnline.toInt()}"
                                    )
                                }
                            }
                        }
                    }
                }

                // Employee Performance Bar Chart
                if (empEarnings.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = Strings.get("employee_performance", lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val maxAmt = (empEarnings.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                                val barColor = MaterialTheme.colorScheme.primary

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    empEarnings.forEach { (name, amt) ->
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                Text("$currencySym ${amt.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(fraction = (amt / maxAmt).toFloat())
                                                        .clip(RoundedCornerShape(5.dp))
                                                        .background(barColor)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Expense Category Breakdown
                if (expenseCategories.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = Strings.get("category_breakdown", lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val categoryColors = listOf(
                                    Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B),
                                    Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    expenseCategories.entries.toList().forEachIndexed { index, (cat, amt) ->
                                        val color = categoryColors[index % categoryColors.size]
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(cat, style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Text(
                                                "$currencySym ${amt.toInt()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(amount, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}
