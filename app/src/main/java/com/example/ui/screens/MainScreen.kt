package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SubtleBackgroundAnimation
import com.example.ui.viewmodel.SalonViewModel
import com.example.utils.Strings

enum class NavTab(val titleKey: String, val icon: ImageVector, val tag: String) {
    HOME("nav_home", Icons.Default.Home, "tab_home"),
    ANALYSIS("nav_analysis", Icons.Default.BarChart, "tab_analysis"),
    EMPLOYEES("nav_employees", Icons.Default.People, "tab_employees"),
    EXPENSES("nav_expenses", Icons.Default.ReceiptLong, "tab_expenses"),
    REPORTS("nav_reports", Icons.Default.Assessment, "tab_reports"),
    SETTINGS("nav_settings", Icons.Default.Settings, "tab_settings")
}

@Composable
fun MainScreen(viewModel: SalonViewModel) {
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeEmployees by viewModel.activeEmployees.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val todayEntries by viewModel.todayEntries.collectAsStateWithLifecycle()
    val todayExpenses by viewModel.todayExpenses.collectAsStateWithLifecycle()
    val allExpenses by viewModel.allExpenses.collectAsStateWithLifecycle()
    val allDailyEntries by viewModel.allDailyEntries.collectAsStateWithLifecycle()
    val voiceFeedback by viewModel.voiceFeedback.collectAsStateWithLifecycle()

    val lang = settings.language
    val layoutDirection = if (lang == "UR" || lang == "AR") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (!settings.isSetupCompleted) {
            SetupScreen(
                currentLang = lang,
                onSetupComplete = { sName, oName, curr, hrs ->
                    viewModel.completeSetup(sName, oName, curr, hrs)
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("main_bottom_nav"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            NavTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = Strings.get(tab.titleKey, lang)) },
                                    label = {
                                        Text(
                                            text = Strings.get(tab.titleKey, lang),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "MADE BY AR WORDS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Subtle dynamic animated background
                    SubtleBackgroundAnimation(modifier = Modifier.fillMaxSize())

                    Crossfade(
                        targetState = selectedTab,
                        label = "TabCrossfade"
                    ) { tab ->
                    when (tab) {
                        NavTab.HOME -> HomeScreen(
                            settings = settings,
                            selectedDate = selectedDate,
                            activeEmployees = activeEmployees,
                            todayEntries = todayEntries,
                            todayExpenses = todayExpenses,
                            voiceFeedback = voiceFeedback,
                            onDateOffset = { offset -> viewModel.navigateDate(offset) },
                            onSaveEmployeeEntry = { id, name, cash, online, breakdown ->
                                viewModel.saveEmployeeDailyEntry(id, name, cash, online, breakdown)
                            },
                            onAddExpense = { name, cat, amount, notes ->
                                viewModel.addExpense(name, cat, amount, notes)
                            },
                            onProcessVoice = { text -> viewModel.processVoiceCommand(text) },
                            onClearVoiceFeedback = { viewModel.clearVoiceFeedback() }
                        )

                        NavTab.ANALYSIS -> AnalysisScreen(
                            settings = settings,
                            allEmployees = allEmployees,
                            allDailyEntries = allDailyEntries,
                            allExpenses = allExpenses
                        )

                        NavTab.EMPLOYEES -> EmployeesScreen(
                            settings = settings,
                            allEmployees = allEmployees,
                            onAddEmployee = { name, role, phone ->
                                viewModel.addEmployee(name, role, phone)
                            },
                            onUpdateEmployee = { emp -> viewModel.updateEmployee(emp) },
                            onDeleteEmployee = { emp -> viewModel.deleteEmployee(emp) }
                        )

                        NavTab.EXPENSES -> ExpensesScreen(
                            settings = settings,
                            allExpenses = allExpenses,
                            onAddExpense = { name, cat, amount, notes ->
                                viewModel.addExpense(name, cat, amount, notes)
                            },
                            onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
                        )

                        NavTab.REPORTS -> ReportsScreen(
                            settings = settings,
                            allDailyEntries = allDailyEntries,
                            allExpenses = allExpenses
                        )

                        NavTab.SETTINGS -> SettingsScreen(
                            settings = settings,
                            onUpdateSettings = { sName, oName, curr, hrs, thm, lng, sUrl, sKey, sEnabled ->
                                viewModel.updateSettings(sName, oName, curr, hrs, thm, lng, sUrl, sKey, sEnabled)
                            },
                            onTestSupabaseConnection = { url, key -> viewModel.testSupabaseConnection(url, key) },
                            onSyncAllToSupabase = { viewModel.syncAllDataToSupabase() },
                            onExportBackup = { viewModel.exportBackupJson() },
                            onRestoreBackup = { json -> viewModel.restoreBackupJson(json) }
                        )
                    }
                }
            }
        }
    }
}
}
