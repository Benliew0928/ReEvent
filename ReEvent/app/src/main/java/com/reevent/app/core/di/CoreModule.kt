package com.reevent.app.core.di

import android.content.Context
import androidx.room.Room
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.LocalFirstCoreRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.MediaRepository
import com.reevent.app.core.data.SupabaseMediaRepository
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.ReEventDatabase
import com.reevent.app.core.auth.DefaultAuthRepository
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
            .addMigrations(ReEventDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideCoreDao(database: ReEventDatabase): CoreDao = database.coreDao()

    @Provides fun provideEventRepository(repository: LocalFirstCoreRepository): EventRepository = repository
    @Provides fun provideAuthRepository(repository: DefaultAuthRepository): AuthRepository = repository
    @Provides fun provideResourceRepository(repository: LocalFirstCoreRepository): ResourceRepository = repository
    @Provides fun providePassportRepository(repository: LocalFirstCoreRepository): PassportRepository = repository
    @Provides fun providePartnerRepository(repository: LocalFirstCoreRepository): PartnerRepository = repository
    @Provides fun provideTransactionRepository(repository: LocalFirstCoreRepository): TransactionRepository = repository
    @Provides fun provideImpactRepository(repository: LocalFirstCoreRepository): ImpactRepository = repository
    @Provides fun provideCoreSyncRepository(repository: LocalFirstCoreRepository): CoreSyncRepository = repository
    @Provides fun provideMediaRepository(repository: SupabaseMediaRepository): MediaRepository = repository
}
