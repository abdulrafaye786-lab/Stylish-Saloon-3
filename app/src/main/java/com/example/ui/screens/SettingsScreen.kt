package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SalonSettingsEntity
import com.example.utils.Language
import com.example.utils.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SalonSettingsEntity,
    onUpdateSettings: (salonName: String, ownerName: String, currency: String, hours: String, theme: String, language: String) -> Unit,
    onExportBackup: suspend () -> String,
    onRestoreBackup: suspend (String) -> Unit
) {
    var salonName by remember(settings.salonName) { mutableStateOf(settings.salonName) }
    var ownerName by remember(settings.ownerName) { mutableStateOf(settings.ownerName) }
    var currency by remember(settings.currency) { mutableStateOf(settings.currency) }
    var workingHours by remember(settings.workingHours) { mutableStateOf(settings.workingHours) }
    var appTheme by remember(settings.appTheme) { mutableStateOf(settings.appTheme) }
    var language by remember(settings.language) { mutableStateOf(settings.language) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lang = settings.language

    val currencies = listOf("PKR", "USD", "EUR", "INR", "GBP", "AED", "SAR")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Strings.get("settings_title", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Salon Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = Strings.get("salon_details", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = salonName,
                        onValueChange = { salonName = it },
                        label = { Text(Strings.get("setup_salon_name", lang)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_salon_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(Strings.get("setup_owner_name", lang)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_owner_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = { workingHours = it },
                        label = { Text(Strings.get("setup_hours", lang)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Theme & Language Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = Strings.get("appearance", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Column {
                        Text(
                            text = Strings.get("appearance", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SYSTEM", "LIGHT", "DARK").forEach { t ->
                                FilterChip(
                                    selected = appTheme == t,
                                    onClick = {
                                        appTheme = t
                                        onUpdateSettings(salonName, ownerName, currency, workingHours, t, language)
                                    },
                                    label = {
                                        Text(
                                            when (t) {
                                                "LIGHT" -> Strings.get("theme_light", lang)
                                                "DARK" -> Strings.get("theme_dark", lang)
                                                else -> Strings.get("theme_system", lang)
                                            }
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = Strings.get("language", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Language.entries.forEach { l ->
                                FilterChip(
                                    selected = language == l.code,
                                    onClick = {
                                        language = l.code
                                        onUpdateSettings(salonName, ownerName, currency, workingHours, appTheme, l.code)
                                    },
                                    label = { Text(l.displayName, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = Strings.get("setup_currency", lang),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currencies.take(4).forEach { cur ->
                                FilterChip(
                                    selected = currency == cur,
                                    onClick = {
                                        currency = cur
                                        onUpdateSettings(salonName, ownerName, cur, workingHours, appTheme, language)
                                    },
                                    label = { Text(cur, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Save Settings Button
            Button(
                onClick = {
                    onUpdateSettings(salonName, ownerName, currency, workingHours, appTheme, language)
                    Toast.makeText(context, "Settings Saved!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_btn"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.get("save", lang), fontWeight = FontWeight.Bold)
            }

            // Data Backup & Restore
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = Strings.get("backup_restore", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = onExportBackup()
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, json)
                                        type = "application/json"
                                    }
                                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Export Backup JSON"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.get("backup_json", lang), fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Strings.get("restore_json", lang), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Security & Roles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = Strings.get("security_roles", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Strings.get("current_role", lang),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Footer
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = Strings.get("footer_text", lang),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(Strings.get("restore_json", lang)) },
            text = {
                Column {
                    Text("Paste JSON backup content to restore database:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            coroutineScope.launch {
                                onRestoreBackup(restoreJsonText)
                                showRestoreDialog = false
                                Toast.makeText(context, Strings.get("restore_completed", lang), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(Strings.get("confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(Strings.get("cancel", lang))
                }
            }
        )
    }
}
