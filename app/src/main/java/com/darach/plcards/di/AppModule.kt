package com.darach.plcards.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.allowHardware
import com.darach.plcards.data.local.CardDatabase
import com.darach.plcards.data.local.dao.CardDao
import com.darach.plcards.data.local.dao.RecentSearchDao
import com.darach.plcards.data.remote.ApiService
import com.darach.plcards.data.repository.CardRepositoryImpl
import com.darach.plcards.data.repository.SettingsRepository
import com.darach.plcards.domain.repository.CardRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        client: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(client))
            }
            .allowHardware(true) // Allow hardware acceleration for best performance
            .build()
    }

    @Provides
    @Singleton
    fun provideCardDatabase(app: Application): CardDatabase {
        return Room.databaseBuilder(
            app,
            CardDatabase::class.java,
            "plcards.db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    @Singleton
    fun provideCardDao(db: CardDatabase): CardDao = db.cardDao()

    @Provides
    @Singleton
    fun provideRecentSearchDao(db: CardDatabase) = db.recentSearchDao()

    @Provides
    @Singleton
    fun provideCardRepository(cardDao: CardDao, recentSearchDao: RecentSearchDao): CardRepository {
        return CardRepositoryImpl(cardDao, recentSearchDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}