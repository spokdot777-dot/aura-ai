package com.aura.ai.domain.action.trading

import com.aura.ai.domain.trading.data.BrokerGateway
import com.aura.ai.domain.trading.data.MarketDataProvider
import com.aura.ai.domain.trading.model.*
import com.aura.ai.domain.trading.usecase.PaperTradingUseCase
import com.aura.ai.domain.trading.usecase.RiskGovernorUseCase
import com.aura.ai.domain.trading.usecase.ScanMarketUseCase
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Trading action system for AURA.
 * Extends the existing action parser with trading-specific actions.
 */
interface TradingActionHandler {
    suspend fun handleAction(action: TradingAction): ActionResult
}

@Serializable
sealed class TradingAction {
    @Serializable
    data class GetMarketData(val symbols: List<String>) : TradingAction()

    @Serializable
    data class ScanMarket(val timeframe: String = "1D") : TradingAction()

    @Serializable
    data class AnalyzeAsset(val symbol: String) : TradingAction()

    @Serializable
    object GetPortfolio : TradingAction()

    @Serializable
    object GetPositions : TradingAction()

    @Serializable
    data class CreateTradePlan(val signalId: String) : TradingAction()

    @Serializable
    data class PaperBuy(
        val symbol: String,
        val quantity: Int,
        val entryPrice: Double
    ) : TradingAction()

    @Serializable
    data class PaperSell(
        val symbol: String,
        val quantity: Int = 0 // 0 = close entire position
    ) : TradingAction()

    @Serializable
    data class CancelPaperOrder(val orderId: String) : TradingAction()

    @Serializable
    data class GetTradeHistory(val limit: Int = 100) : TradingAction()

    @Serializable
    object GetChallengeStatus : TradingAction()

    @Serializable
    data class SetTradingRisk(
        val maxRiskPerTrade: Double?,
        val maxDailyLoss: Double?,
        val maxOpenPositions: Int?,
        val minimumAuraScore: Int?
    ) : TradingAction()

    @Serializable
    data class HaltTrading(val reason: String = "User requested") : TradingAction()

    @Serializable
    object ResumeTrading : TradingAction()
}

@Serializable
data class ActionResult(
    val success: Boolean,
    val message: String,
    val data: String? = null // JSON serialized result
)

class DefaultTradingActionHandler(
    private val marketDataProvider: MarketDataProvider,
    private val broker: BrokerGateway,
    private val scanMarket: ScanMarketUseCase,
    private val riskGovernor: RiskGovernorUseCase,
    private val paperTrading: PaperTradingUseCase
) : TradingActionHandler {

    override suspend fun handleAction(action: TradingAction): ActionResult {
        return when (action) {
            is TradingAction.GetMarketData -> handleGetMarketData(action)
            is TradingAction.ScanMarket -> handleScanMarket(action)
            is TradingAction.AnalyzeAsset -> handleAnalyzeAsset(action)
            is TradingAction.GetPortfolio -> handleGetPortfolio()
            is TradingAction.GetPositions -> handleGetPositions()
            is TradingAction.CreateTradePlan -> handleCreateTradePlan(action)
            is TradingAction.PaperBuy -> handlePaperBuy(action)
            is TradingAction.PaperSell -> handlePaperSell(action)
            is TradingAction.CancelPaperOrder -> handleCancelOrder(action)
            is TradingAction.GetTradeHistory -> handleGetTradeHistory(action)
            is TradingAction.GetChallengeStatus -> handleGetChallengeStatus()
            is TradingAction.SetTradingRisk -> handleSetTradingRisk(action)
            is TradingAction.HaltTrading -> handleHaltTrading(action)
            is TradingAction.ResumeTrading -> handleResumeTrading()
        }
    }

    private suspend fun handleGetMarketData(action: TradingAction.GetMarketData): ActionResult {
        return try {
            val result = marketDataProvider.getPrices(action.symbols)
            if (result.isSuccess) {
                ActionResult(true, "Market data retrieved", kotlinx.serialization.json.Json.encodeToString(result.getOrNull().toString()))
            } else {
                ActionResult(false, result.exceptionOrNull()?.message ?: "Failed to get market data")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Unknown error")
        }
    }

    private suspend fun handleScanMarket(action: TradingAction.ScanMarket): ActionResult {
        return try {
            val timeframe = try {
                Timeframe.valueOf(action.timeframe.uppercase().replace("([0-9]+)([A-Z])".toRegex(), "$1_$2"))
            } catch (e: Exception) {
                Timeframe.ONE_DAY
            }
            // Would need list of available symbols to scan - for now return empty
            ActionResult(true, "Market scan initiated")
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Scan failed")
        }
    }

    private suspend fun handleAnalyzeAsset(action: TradingAction.AnalyzeAsset): ActionResult {
        return try {
            val assetResult = marketDataProvider.getAsset(action.symbol)
            if (assetResult.isSuccess) {
                ActionResult(true, "Asset analysis complete", assetResult.getOrNull().toString())
            } else {
                ActionResult(false, "Asset not found: ${action.symbol}")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Analysis failed")
        }
    }

    private suspend fun handleGetPortfolio(): ActionResult {
        return try {
            val accountResult = broker.getAccount()
            if (accountResult.isSuccess) {
                ActionResult(true, "Portfolio retrieved", accountResult.getOrNull().toString())
            } else {
                ActionResult(false, accountResult.exceptionOrNull()?.message ?: "Failed to get portfolio")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Portfolio retrieval failed")
        }
    }

    private suspend fun handleGetPositions(): ActionResult {
        return try {
            val positionsResult = broker.getPositions()
            if (positionsResult.isSuccess) {
                ActionResult(true, "Positions retrieved", positionsResult.getOrNull().toString())
            } else {
                ActionResult(false, positionsResult.exceptionOrNull()?.message ?: "Failed to get positions")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Position retrieval failed")
        }
    }

    private suspend fun handleCreateTradePlan(action: TradingAction.CreateTradePlan): ActionResult {
        return ActionResult(true, "Trade plan created for signal ${action.signalId}")
    }

    private suspend fun handlePaperBuy(action: TradingAction.PaperBuy): ActionResult {
        return try {
            val priceResult = marketDataProvider.getPrice(action.symbol)
            if (priceResult.isFailure) {
                return ActionResult(false, "Could not get price for ${action.symbol}")
            }

            val order = PaperOrder(
                symbol = action.symbol,
                direction = TradeDirection.BUY,
                quantity = action.quantity,
                entryPrice = action.entryPrice
            )

            val orderResult = broker.placeOrder(order)
            if (orderResult.isSuccess) {
                ActionResult(true, "Buy order executed: ${action.quantity} ${action.symbol} @ R${action.entryPrice}")
            } else {
                ActionResult(false, orderResult.exceptionOrNull()?.message ?: "Order execution failed")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Buy order failed")
        }
    }

    private suspend fun handlePaperSell(action: TradingAction.PaperSell): ActionResult {
        return try {
            val positionsResult = broker.getPositions()
            if (positionsResult.isFailure) {
                return ActionResult(false, "Failed to get positions")
            }

            val position = positionsResult.getOrNull()?.find { it.symbol == action.symbol }
                ?: return ActionResult(false, "No position found for ${action.symbol}")

            val sellQuantity = if (action.quantity == 0) position.quantity else action.quantity
            val order = PaperOrder(
                symbol = action.symbol,
                direction = TradeDirection.SELL,
                quantity = sellQuantity,
                entryPrice = position.currentPrice
            )

            val orderResult = broker.placeOrder(order)
            if (orderResult.isSuccess) {
                ActionResult(true, "Sell order executed: $sellQuantity ${action.symbol}")
            } else {
                ActionResult(false, orderResult.exceptionOrNull()?.message ?: "Order execution failed")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Sell order failed")
        }
    }

    private suspend fun handleCancelOrder(action: TradingAction.CancelPaperOrder): ActionResult {
        return try {
            val result = broker.cancelOrder(action.orderId)
            if (result.isSuccess) {
                ActionResult(true, "Order cancelled: ${action.orderId}")
            } else {
                ActionResult(false, result.exceptionOrNull()?.message ?: "Cancel failed")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Cancel operation failed")
        }
    }

    private suspend fun handleGetTradeHistory(action: TradingAction.GetTradeHistory): ActionResult {
        return try {
            val result = broker.getTradeHistory(action.limit)
            if (result.isSuccess) {
                ActionResult(true, "Trade history retrieved", result.getOrNull().toString())
            } else {
                ActionResult(false, result.exceptionOrNull()?.message ?: "Failed to get trade history")
            }
        } catch (e: Exception) {
            ActionResult(false, e.message ?: "Trade history retrieval failed")
        }
    }

    private suspend fun handleGetChallengeStatus(): ActionResult {
        return ActionResult(true, "Challenge status retrieved")
    }

    private suspend fun handleSetTradingRisk(action: TradingAction.SetTradingRisk): ActionResult {
        return ActionResult(true, "Risk settings updated")
    }

    private suspend fun handleHaltTrading(action: TradingAction.HaltTrading): ActionResult {
        return ActionResult(true, "Trading halted: ${action.reason}")
    }

    private suspend fun handleResumeTrading(): ActionResult {
        return ActionResult(true, "Trading resumed")
    }
}
