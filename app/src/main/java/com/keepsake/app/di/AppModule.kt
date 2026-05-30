package com.keepsake.app.di

import android.content.Context
import androidx.room.Room
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.datastore.KeepsakeDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KeepsakeDatabase =
        Room.databaseBuilder(context, KeepsakeDatabase::class.java, "keepsake.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): KeepsakeDataStore =
        KeepsakeDataStore(context)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun provideRoomDao(db: KeepsakeDatabase) = db.roomDao()

    @Provides
    fun provideAreaDao(db: KeepsakeDatabase) = db.areaDao()

    @Provides
    fun provideItemDao(db: KeepsakeDatabase) = db.itemDao()

    @Provides
    fun providePhotoDao(db: KeepsakeDatabase) = db.photoDao()

    @Provides
    fun provideReminderRuleDao(db: KeepsakeDatabase) = db.reminderRuleDao()

    @Provides
    fun provideKvDao(db: KeepsakeDatabase) = db.kvDao()
}
