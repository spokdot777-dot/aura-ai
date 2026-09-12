package com.aura.ai.ui.screen.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.aura.ai.domain.trading.model.*
import java.time.format.DateTimeFormatter

@Composable
fun TradingDashboardScreen(
    portfolio: Portfolio,
    challenge: TradingChallenge?,
    recentSignals: List<TradeSignal>,
    positions: List<Position>,
    tradingStatus: TradingStatus,
    onScanMarket: () -> Unit,
    onOpenPositions: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Bar
        StatusBadge(tradingStatus)

        // Portfolio Summary
        PortfolioSummaryCard(portfolio)

        // Challenge Progress (if active)
        if (challenge != null) {
            ChallengeProgressCard(challenge)
        }

        // Trading Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onScanMarket,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Market")
            }

            Button(
                onClick = onOpenPositions,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Positions")
            }
        }

        // Recent Signals
        if (recentSignals.isNotEmpty()) {
            Text(
                "Recent AI Signals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentSignals.take(5)) { signal ->
                    TradeSignalCard(signal)
                }
            }
        }

        // Settings Button
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Risk Settings")
        }
    }
}

@Composable
private fun StatusBadge(tradingStatus: TradingStatus) {
    val backgroundColor = when {
        tradingStatus.isTradingHalted -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val textColor = when {
        tradingStatus.isTradingHalted -> MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (tradingStatus.isTradingHalted) "⛔ TRADING HALTED" else "✓ TRADING ACTIVE",
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                if (tradingStatus.isTradingHalted) {
                    Text(
                        tradingStatus.haltReason ?: "Unknown reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
            Text(
                "📊 PAPER",
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}

@Composable
private fun PortfolioSummaryCard(portfolio: Portfolio) {
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
                "Portfolio Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "R${String.format("%.2f", portfolio.calculateTotalPortfolioValue())}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Key metrics in grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PortfolioMetricItem(
                    "Starting",
                    "R${String.format("%.0f", portfolio.startingBalance)}",
                    modifier = Modifier.weight(1f)
                )
                PortfolioMetricItem(
                    "Cash",
                    "R${String.format("%.0f", portfolio.availableCash)}",
                    modifier = Modifier.weight(1f)
                )
                PortfolioMetricItem(
                    "Positions",
                    portfolio.positions.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PortfolioMetricItem(
                    "Unrealised P/L",
                    "R${String.format("%.2f", portfolio.totalUnrealisedPnL)}",
                    isPositive = portfolio.totalUnrealisedPnL >= 0,
                    modifier = Modifier.weight(1f)
                )
                PortfolioMetricItem(
                    "Realised P/L",
                    "R${String.format("%.2f", portfolio.totalRealisedPnL)}",
                    isPositive = portfolio.totalRealisedPnL >= 0,
                    modifier = Modifier.weight(1f)
                )
                PortfolioMetricItem(
                    "Win Rate",
                    "${String.format("%.1f", portfolio.winRate * 100)}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PortfolioMetricItem(
    label: String,
    value: String,
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun ChallengeProgressCard(challenge: TradingChallenge) {
    val progress = challenge.getProgressPercent() / 100.0
    val isAhead = challenge.isAheadOfSchedule()
    val daysRemaining = challenge.getDaysRemaining()

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "R100 → R10,000 Challenge",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (isAhead) {
                    Text("🚀 AHEAD OF SCHEDULE", style = MaterialTheme.typography.labelSmall)
                }
            }

            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${String.format("%.1f", challenge.getProgressPercent())}% Complete",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "$daysRemaining days left",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TradeSignalCard(signal: TradeSignal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    signal.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = if (signal.direction == TradeDirection.BUY) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        signal.direction.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    "${signal.auraScore}/100",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (signal.auraScore >= 70) Color(0xFF4CAF50) else Color(0xFFFFC107)
                )
            }
            Text(
                signal.reason,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Entry: ${String.format("%.2f", signal.entryPrice)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "Stop: ${String.format("%.2f", signal.stopLoss)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    "R/R: ${String.format("%.2f", signal.riskRewardRatio)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
