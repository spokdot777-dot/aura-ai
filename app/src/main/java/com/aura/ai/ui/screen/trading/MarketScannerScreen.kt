package com.aura.ai.ui.screen.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.ai.domain.trading.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScannerScreen(
    signals: List<TradeSignal>,
    isScanning: Boolean,
    onScan: () -> Unit,
    onSelectSignal: (TradeSignal) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TopAppBar(
            title = { Text("Market Scanner") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scan Button
            Button(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isScanning) "Scanning..." else "Scan Now")
            }

            // Info
            if (signals.isEmpty() && !isScanning) {
                Text(
                    "No signals yet. Tap 'Scan Now' to find trading opportunities.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Signals List
            if (signals.isNotEmpty()) {
                Text(
                    "${signals.size} Signals Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(signals) { signal ->
                        ScannerSignalCard(
                            signal = signal,
                            onClick = { onSelectSignal(signal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerSignalCard(
    signal: TradeSignal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        signal.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        signal.market,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = if (signal.direction == TradeDirection.BUY) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        signal.direction.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                signal.reason,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AURA SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${signal.auraScore}/100",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = getScoreColor(signal.auraScore)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "R/R RATIO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        String.format("%.2f", signal.riskRewardRatio),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .height(32.dp)
                        .width(80.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("View", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFF2196F3) // Blue
        score >= 40 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336) // Red
    }
}
