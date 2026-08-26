package com.reevent.app.core.di

import android.content.Context
import androidx.room.Room
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.GeocodingRepository
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.LocalFirstCoreRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.MediaRepository
import com.reevent.app.core.data.MarketplaceListingRepository
import com.reevent.app.core.data.SupabaseMediaRepository
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.ReEventDatabase
import com.reevent.app.core.auth.DefaultAuthRepository
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.network.SupabaseAuthGateway
import com.reevent.app.core.network.LifecycleCommandGateway
import com.reevent.app.core.network.SupabaseLifecycleCommandGateway
import com.reevent.app.core.network.SupabaseGeocodingRepository
import com.reevent.app.core.sync.SyncGateway
import com.reevent.app.core.sync.AccountSyncScheduler
import com.reevent.app.core.sync.SyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReEventDatabase =
        Room.databaseBuilder(context, ReEventDatabase::class.java, "reevent.db")
            .addMigrations(
                ReEventDatabase.MIGRATION_1_2,
                ReEventDatabase.MIGRATION_2_3,
                ReEventDatabase.MIGRATION_3_4,
                ReEventDatabase.MIGRATION_4_5,
                ReEventDatabase.MIGRATION_5_6,
                ReEventDatabase.MIGRATION_6_7,
                ReEventDatabase.MIGRATION_7_8,
                ReEventDatabase.MIGRATION_8_9,
            )
            .build()

    @Provides
    fun provideCoreDao(database: ReEventDatabase): CoreDao = database.coreDao()

    @Provides
    @Singleton
    fun provideAppEnvironment(): AppEnvironment = AppEnvironment.current

    @Provides
    fun provideSyncGateway(gateway: SupabaseAuthGateway): SyncGateway = gateway

    @Provides
    fun provideLifecycleCommandGateway(gateway: SupabaseLifecycleCommandGateway): LifecycleCommandGateway = gateway

    @Provides
    fun provideAccountSyncScheduler(scheduler: SyncScheduler): AccountSyncScheduler = scheduler

    @Provides fun provideEventRepository(repository: LocalFirstCoreRepository): EventRepository = repository
    @Provides fun provideAuthRepository(repository: DefaultAuthRepository): AuthRepository = repository
    @Provides fun provideResourceRepository(repository: LocalFirstCoreRepository): ResourceRepository = repository
    @Provides fun provideMarketplaceListingRepository(repository: LocalFirstCoreRepository): MarketplaceListingRepository = repository
    @Provides fun providePassportRepository(repository: LocalFirstCoreRepository): PassportRepository = repository
    @Provides fun providePartnerRepository(repository: LocalFirstCoreRepository): PartnerRepository = repository
    @Provides fun provideTransactionRepository(repository: LocalFirstCoreRepository): TransactionRepository = repository
    @Provides fun provideImpactRepository(repository: LocalFirstCoreRepository): ImpactRepository = repository
    @Provides fun provideCoreSyncRepository(repository: LocalFirstCoreRepository): CoreSyncRepository = repository
    @Provides fun provideMediaRepository(repository: SupabaseMediaRepository): MediaRepository = repository
    @Provides fun provideGeocodingRepository(repository: SupabaseGeocodingRepository): GeocodingRepository = repository
}
