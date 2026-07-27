package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    currentLang: String,
    onSetupComplete: (salonName: String, ownerName: String, currency: String, hours: String) -> Unit
) {
    var salonName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("PKR") }
    var workingHours by remember { mutableStateOf("09:00 AM - 09:00 PM") }

    val currencies = listOf("PKR", "USD", "EUR", "INR", "GBP", "AED", "SAR")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Salon Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = Strings.get("setup_title", currentLang),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = Strings.get("setup_subtitle", currentLang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = salonName,
                        onValueChange = { salonName = it },
                        label = { Text(Strings.get("setup_salon_name", currentLang)) },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_salon_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(Strings.get("setup_owner_name", currentLang)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_owner_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Column {
                        Text(
                            text = Strings.get("setup_currency", currentLang),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currencies.take(4).forEach { cur ->
                                FilterChip(
                                    selected = currency == cur,
                                    onClick = { currency = cur },
                                    label = { Text(cur, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = workingHours,
                        onValueChange = { workingHours = it },
                        label = { Text(Strings.get("setup_hours", currentLang)) },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_hours_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            onSetupComplete(
                                salonName.ifBlank { "Crown Salon" },
                                ownerName.ifBlank { "Owner" },
                                currency,
                                workingHours.ifBlank { "09:00 AM - 09:00 PM" }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("setup_submit_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = Strings.get("setup_start", currentLang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
