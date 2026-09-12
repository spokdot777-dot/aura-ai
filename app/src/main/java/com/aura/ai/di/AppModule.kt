package com.aura.ai.di

import android.content.Context
import androidx.room.Room
import com.aura.ai.BuildConfig
import com.aura.ai.data.database.AuraDatabase
import com.aura.ai.data.database.dao.*
import com.aura.ai.data.auth.CoreTokenStore
import com.aura.ai.data.remote.OllamaAiProvider
import com.aura.ai.data.remote.OllamaConfiguration
import com.aura.ai.data.remote.OllamaService
import com.aura.ai.domain.ai.AiProvider
import com.aura.ai.data.remote.core.CoreAuthInterceptor
import com.aura.ai.data.remote.core.CoreService
import com.aura.ai.network.BodySanitizingInterceptor
import com.aura.ai.network.RedactingLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("ollama")
    fun provideOllamaOkHttpClient(): OkHttpClient {
        val redacting = RedactingLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(BodySanitizingInterceptor())
            .addInterceptor(redacting)
            .connectTimeout(OllamaConfiguration.connectionTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(OllamaConfiguration.readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(OllamaConfiguration.writeTimeoutSeconds, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("core")
    fun provideCoreOkHttpClient(coreAuthInterceptor: CoreAuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(coreAuthInterceptor)
            .addInterceptor(RedactingLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT))
            .build()

    @Provides
    @Singleton
    fun provideCoreService(@Named("core") coreOkHttpClient: OkHttpClient, moshi: Moshi): CoreService =
        Retrofit.Builder()
            .baseUrl(BuildConfig.CORE_BASE_URL.trimEnd('/') + "/")
            .client(coreOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CoreService::class.java)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideAiProvider(ollamaAiProvider: OllamaAiProvider): AiProvider = ollamaAiProvider

    @Provides
    @Singleton
    fun provideOllamaService(@Named("ollama") okHttpClient: OkHttpClient, moshi: Moshi): OllamaService {
        return Retrofit.Builder()
            .baseUrl(OllamaConfiguration.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OllamaService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuraDatabase(@ApplicationContext context: Context): AuraDatabase {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skills` (
                      `name` TEXT NOT NULL,
                      `purpose` TEXT NOT NULL,
                      `currentVersion` TEXT NOT NULL,
                      `active` INTEGER NOT NULL,
                      `createdAt` INTEGER NOT NULL,
                      `updatedAt` INTEGER NOT NULL,
                      PRIMARY KEY(`name`)
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skill_versions` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `metadataJson` TEXT NOT NULL,
                      `codeRef` TEXT,
                      `createdAt` INTEGER NOT NULL,
                      `testPassed` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skill_stats` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `successfulRuns` INTEGER NOT NULL,
                      `failedRuns` INTEGER NOT NULL,
                      `averageConfidence` REAL NOT NULL,
                      `lastObservedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_events` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `eventType` TEXT NOT NULL,
                      `sourceSkill` TEXT,
                      `detailsJson` TEXT NOT NULL,
                      `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `improvement_proposals` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `title` TEXT NOT NULL,
                      `skillName` TEXT,
                      `description` TEXT NOT NULL,
                      `proposalJson` TEXT NOT NULL,
                      `createdBy` TEXT NOT NULL,
                      `approved` INTEGER NOT NULL,
                      `approvedBy` TEXT,
                      `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sandbox_test_results` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `passed` INTEGER NOT NULL,
                      `metricsJson` TEXT NOT NULL,
                      `runAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `permission_requests` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `permission` TEXT NOT NULL,
                      `reason` TEXT NOT NULL,
                      `requestedBy` TEXT NOT NULL,
                      `granted` INTEGER NOT NULL,
                      `handledAt` INTEGER,
                      `requestedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_log` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `eventType` TEXT NOT NULL,
                      `eventSource` TEXT NOT NULL,
                      `detailsJson` TEXT NOT NULL,
                      `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        return Room.databaseBuilder(
            context,
            AuraDatabase::class.java,
            "aura_database"
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideConversationDao(db: AuraDatabase): com.aura.ai.data.database.dao.ConversationDao = db.conversationDao()

    @Provides
    fun provideMemoryDao(db: AuraDatabase): com.aura.ai.data.database.dao.MemoryDao = db.memoryDao()

    @Provides
    fun provideSkillDao(db: AuraDatabase): com.aura.ai.data.database.dao.SkillDao = db.skillDao()

    @Provides
    fun provideSkillVersionDao(db: AuraDatabase): com.aura.ai.data.database.dao.SkillVersionDao = db.skillVersionDao()

    @Provides
    fun provideSkillStatDao(db: AuraDatabase): com.aura.ai.data.database.dao.SkillStatDao = db.skillStatDao()

    @Provides
    fun provideLearningEventDao(db: AuraDatabase): com.aura.ai.data.database.dao.LearningEventDao = db.learningEventDao()

    @Provides
    fun provideAuditLogDao(db: AuraDatabase): com.aura.ai.data.database.dao.AuditLogDao = db.auditLogDao()

    @Provides
    fun provideImprovementProposalDao(db: AuraDatabase): com.aura.ai.data.database.dao.ImprovementProposalDao = db.improvementProposalDao()

    @Provides
    fun provideSandboxTestResultDao(db: AuraDatabase): com.aura.ai.data.database.dao.SandboxTestResultDao = db.sandboxTestResultDao()

    @Provides
    fun providePermissionRequestDao(db: AuraDatabase): com.aura.ai.data.database.dao.PermissionRequestDao = db.permissionRequestDao()
}
