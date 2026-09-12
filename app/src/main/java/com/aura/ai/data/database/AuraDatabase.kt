package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.database.dao.*
import com.aura.ai.data.database.entity.*
import com.aura.ai.data.database.trading.*

@Database(
    entities = [
        ConversationEntity::class,
        MemoryEntity::class,
        SkillEntity::class,
        SkillVersionEntity::class,
        SkillStatEntity::class,
        LearningEventEntity::class,
        AuditLogEntity::class,
        ImprovementProposalEntity::class,
        SandboxTestResultEntity::class,
        PermissionRequestEntity::class,
        // trading entities
        MarketAssetEntity::class,
        PriceSnapshotEntity::class,
        TechnicalIndicatorsEntity::class,
        TradeSignalEntity::class,
        TradePlanEntity::class,
        PaperOrderEntity::class,
        PositionEntity::class,
        PortfolioEntity::class,
        TradingChallengeEntity::class,
        TradingSettingsEntity::class,
        TradingPerformanceEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun conversationDao(): com.aura.ai.data.database.dao.ConversationDao
    abstract fun memoryDao(): com.aura.ai.data.database.dao.MemoryDao

    abstract fun skillDao(): SkillDao
    abstract fun skillVersionDao(): SkillVersionDao
    abstract fun skillStatDao(): SkillStatDao
    abstract fun learningEventDao(): LearningEventDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun improvementProposalDao(): ImprovementProposalDao
    abstract fun sandboxTestResultDao(): SandboxTestResultDao
    abstract fun permissionRequestDao(): PermissionRequestDao

    // trading DAOs
    abstract fun marketAssetDao(): MarketAssetDao
    abstract fun priceSnapshotDao(): PriceSnapshotDao
    abstract fun technicalIndicatorsDao(): TechnicalIndicatorsDao
    abstract fun tradeSignalDao(): TradeSignalDao
    abstract fun tradePlanDao(): TradePlanDao
    abstract fun paperOrderDao(): PaperOrderDao
    abstract fun positionDao(): PositionDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun tradingChallengeDao(): TradingChallengeDao
    abstract fun tradingSettingsDao(): TradingSettingsDao
    abstract fun tradingPerformanceDao(): TradingPerformanceDao
}
