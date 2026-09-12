package com.aura.ai.ui.screen.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.ai.domain.trading.model.*

@Composable
fun PortfolioScreen(
    portfolio: Portfolio,
    positions: List<Position>,
    onPositionClose: (Position) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple header row with back button and title (avoid TopAppBar to sidestep experimental API)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Portfolio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                PortfolioDetailCard(portfolio)
            }

            if (positions.isNotEmpty()) {
                item {
                    Text(
                        "Open Positions (${positions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(positions) { position ->
                    PositionCard(
                        position = position,
                        onClose = { onPositionClose(position) }
                    )
                }
            } else {
                item {
                    Text(
                        "No open positions",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioDetailCard(portfolio: Portfolio) {
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
            // Total Value
            Column {
                Text(
                    "Total Portfolio Value",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "R${String.format("%.2f", portfolio.calculateTotalPortfolioValue())}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Divider()

            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        "Starting",
                        "R${String.format("%.0f", portfolio.startingBalance)}",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        "Available Cash",
                        "R${String.format("%.0f", portfolio.availableCash)}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        "Unrealised P/L",
                        "R${String.format("%.2f", portfolio.totalUnrealisedPnL)}",
                        isPositive = portfolio.totalUnrealisedPnL >= 0,
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        "Realised P/L",
                        "R${String.format("%.2f", portfolio.totalRealisedPnL)}",
                        isPositive = portfolio.totalRealisedPnL >= 0,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatItem(
                        "Total Return",
                        "${String.format("%.2f", portfolio.totalReturnPercent)}%",
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        "Win Rate",
                        "${String.format("%.1f", portfolio.winRate * 100)}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionCard(
    position: Position,
    onClose: () -> Unit
) {
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
                Column {
                    Text(
                        position.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${position.quantity} units",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    "R${String.format("%.2f", position.unrealisedPnL)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (position.unrealisedPnL >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "R${String.format("%.2f", position.entryPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "R${String.format("%.2f", position.currentPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Return",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${String.format("%.2f", position.unrealisedPnLPercent)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (position.unrealisedPnL >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            Button(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.End)
                    .height(32.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text("Close", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatItem(
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}
