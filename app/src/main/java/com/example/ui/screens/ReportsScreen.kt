package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.DailyEntryEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    settings: SalonSettingsEntity,
    allDailyEntries: List<DailyEntryEntity>,
    allExpenses: List<ExpenseEntity>
) {
    var selectedTimeframe by remember { mutableStateOf("Daily") }
    val timeframes = listOf("Daily", "Weekly", "Monthly", "Yearly")

    val lang = settings.language
    val currencySym = settings.currencySymbol
    val context = LocalContext.current

    val totalCash = allDailyEntries.sumOf { it.cashEarnings }
    val totalOnline = allDailyEntries.sumOf { it.onlinePayments }
    val totalRevenue = totalCash + totalOnline
    val totalExpenses = allExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenses

    val empEarnings = remember(allDailyEntries) {
        allDailyEntries.groupBy { it.employeeName }
            .mapValues { entry -> entry.value.sumOf { it.cashEarnings + it.onlinePayments } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("reports_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Timeframe Selector Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeframes.forEach { tf ->
                        FilterChip(
                            selected = selectedTimeframe == tf,
                            onClick = { selectedTimeframe = tf },
                            label = { Text(Strings.get(tf.lowercase(), lang).ifEmpty { tf }) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Summary Statement Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = settings.salonName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Statement • $selectedTimeframe",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 14.dp))

                        ReportRow(label = Strings.get("cash_revenue", lang), value = "$currencySym ${totalCash.toInt()}")
                        ReportRow(label = Strings.get("online_revenue", lang), value = "$currencySym ${totalOnline.toInt()}")
                        ReportRow(label = Strings.get("total_revenue", lang), value = "$currencySym ${totalRevenue.toInt()}", isBold = true)
                        ReportRow(label = Strings.get("today_expenses", lang), value = "- $currencySym ${totalExpenses.toInt()}", isError = true)

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        ReportRow(
                            label = Strings.get("today_profit", lang),
                            value = "$currencySym ${netProfit.toInt()}",
                            isBold = true,
                            isHighlighted = true
                        )
                    }
                }
            }

            // Employee Breakdown Section
            if (empEarnings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = Strings.get("employee_performance", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            empEarnings.forEach { (empName, amt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(empName, style = MaterialTheme.typography.bodyMedium)
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

            // Export Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val textReport = generateReportText(settings, selectedTimeframe, totalRevenue, totalCash, totalOnline, totalExpenses, netProfit, empEarnings)
                            shareReportIntent(context, textReport)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_pdf_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.get("export_pdf", lang))
                    }

                    OutlinedButton(
                        onClick = {
                            val csvReport = generateCsvText(allDailyEntries, allExpenses)
                            shareReportIntent(context, csvReport, mimeType = "text/csv")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("export_csv_btn"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.get("export_csv", lang))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isError: Boolean = false,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = when {
                isHighlighted -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun generateReportText(
    settings: SalonSettingsEntity,
    timeframe: String,
    revenue: Double,
    cash: Double,
    online: Double,
    expenses: Double,
    profit: Double,
    empEarnings: Map<String, Double>
): String {
    val sb = java.lang.StringBuilder()
    sb.appendLine("========== ${settings.salonName.uppercase()} ==========")
    sb.appendLine("Owner: ${settings.ownerName}")
    sb.appendLine("Report Type: $timeframe Statement")
    sb.appendLine("Currency: ${settings.currencySymbol} (${settings.currency})")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine("Total Revenue:   ${settings.currencySymbol} ${revenue.toInt()}")
    sb.appendLine(" - Cash:         ${settings.currencySymbol} ${cash.toInt()}")
    sb.appendLine(" - Online:       ${settings.currencySymbol} ${online.toInt()}")
    sb.appendLine("Total Expenses:  ${settings.currencySymbol} ${expenses.toInt()}")
    sb.appendLine("NET PROFIT:      ${settings.currencySymbol} ${profit.toInt()}")
    sb.appendLine("--------------------------------------------------")
    sb.appendLine("EMPLOYEE EARNINGS BREAKDOWN:")
    empEarnings.forEach { (name, amt) ->
        sb.appendLine(" - $name: ${settings.currencySymbol} ${amt.toInt()}")
    }
    sb.appendLine("==================================================")
    sb.appendLine("Generated by Salon Register Application")
    return sb.toString()
}

private fun generateCsvText(
    entries: List<DailyEntryEntity>,
    expenses: List<ExpenseEntity>
): String {
    val sb = java.lang.StringBuilder()
    sb.appendLine("Date,Employee,Cash Earnings,Online Payments,Total")
    entries.forEach {
        sb.appendLine("${it.date},\"${it.employeeName}\",${it.cashEarnings},${it.onlinePayments},${it.cashEarnings + it.onlinePayments}")
    }
    sb.appendLine("\nDate,Expense Name,Category,Amount,Notes")
    expenses.forEach {
        sb.appendLine("${it.date},\"${it.name}\",\"${it.category}\",${it.amount},\"${it.notes}\"")
    }
    return sb.toString()
}

private fun shareReportIntent(context: Context, text: String, mimeType: String = "text/plain") {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = mimeType
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Report")
    context.startActivity(shareIntent)
}
