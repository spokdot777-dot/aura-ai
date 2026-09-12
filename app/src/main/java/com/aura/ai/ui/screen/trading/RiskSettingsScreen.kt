package com.aura.ai.ui.screen.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.ai.domain.trading.model.RiskGovernor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskSettingsScreen(
    riskSettings: RiskGovernor,
    onSettingsUpdate: (RiskGovernor) -> Unit,
    onBackClick: () -> Unit
) {
    var maxRiskPerTrade by remember { mutableStateOf(riskSettings.maxRiskPerTrade.toString()) }
    var maxDailyLoss by remember { mutableStateOf(riskSettings.maxDailyLoss.toString()) }
    var maxOpenPositions by remember { mutableStateOf(riskSettings.maxOpenPositions.toString()) }
    var maxPositionSize by remember { mutableStateOf(riskSettings.maxPositionSize.toString()) }
    var minimumAuraScore by remember { mutableStateOf(riskSettings.minimumAuraScore.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Risk Management") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "⚠️  Risk Controls",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "These settings limit trade size and frequency to protect your capital.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                RiskInputField(
                    label = "Max Risk Per Trade (R)",
                    value = maxRiskPerTrade,
                    onValueChange = { maxRiskPerTrade = it }
                )
            }

            item {
                RiskInputField(
                    label = "Max Daily Loss (R)",
                    value = maxDailyLoss,
                    onValueChange = { maxDailyLoss = it }
                )
            }

            item {
                RiskInputField(
                    label = "Max Open Positions",
                    value = maxOpenPositions,
                    onValueChange = { maxOpenPositions = it }
                )
            }

            item {
                RiskInputField(
                    label = "Max Position Size (units)",
                    value = maxPositionSize,
                    onValueChange = { maxPositionSize = it }
                )
            }

            item {
                RiskInputField(
                    label = "Minimum AURA Score (0-100)",
                    value = minimumAuraScore,
                    onValueChange = { minimumAuraScore = it }
                )
            }

            item {
                Button(
                    onClick = {
                        try {
                            val updated = riskSettings.copy(
                                maxRiskPerTrade = maxRiskPerTrade.toDoubleOrNull() ?: riskSettings.maxRiskPerTrade,
                                maxDailyLoss = maxDailyLoss.toDoubleOrNull() ?: riskSettings.maxDailyLoss,
                                maxOpenPositions = maxOpenPositions.toIntOrNull() ?: riskSettings.maxOpenPositions,
                                maxPositionSize = maxPositionSize.toIntOrNull() ?: riskSettings.maxPositionSize,
                                minimumAuraScore = minimumAuraScore.toIntOrNull() ?: riskSettings.minimumAuraScore
                            )
                            onSettingsUpdate(updated)
                        } catch (e: Exception) {
                            // Handle error
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Settings")
                }
            }
        }
    }
}

@Composable
private fun RiskInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
