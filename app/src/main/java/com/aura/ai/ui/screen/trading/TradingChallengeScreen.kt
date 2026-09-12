package com.aura.ai.ui.screen.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.ai.domain.trading.model.TradingChallenge
import com.aura.ai.domain.trading.usecase.ChallengeProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingChallengeScreen(
    challenge: TradingChallenge?,
    progress: ChallengeProgress?,
    onStartChallenge: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Trading Challenge") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (challenge == null) {
            // Start Challenge Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = Color(0xFFFFC107)
                )
                Text(
                    "R100 → R10,000 Challenge",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "6-week paper trading challenge",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Challenge Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "⚠️ This is a TARGET, not a guarantee. The R100→R10,000 goal is highly ambitious and requires significant trading skill and favorable market conditions.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "🎯 Starting Capital: R100 (paper trading only)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "🚀 Target: R10,000 (100x return)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "⏰ Duration: 6 weeks (42 days)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = onStartChallenge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Start Challenge")
                }
            }
        } else if (progress != null) {
            // Challenge Progress Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Progress: ${String.format("%.1f", progress.progressPercent)}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = (progress.progressPercent / 100.0).toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "R${String.format("%.2f", progress.currentReturn)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (progress.currentReturn >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            Text(
                                "${progress.daysRemaining} days left",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Status
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress.isAheadOfSchedule) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFFFC107).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (progress.isAheadOfSchedule) "🚀 AHEAD OF SCHEDULE" else "⏱️ ON SCHEDULE",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Challenge Metrics
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Target vs Actual",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricBox(
                                "Required Return",
                                "${String.format("%.1f", progress.requiredTotalReturnPercent)}%",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                "Current Return",
                                "${String.format("%.2f", progress.currentReturnPercent)}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricBox(
                                "Required CAGR",
                                "${String.format("%.1f", progress.requiredCAGR)}%",
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                "Required Daily",
                                "${String.format("%.2f", progress.requiredDailyReturn)}%",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
