package com.example.zapzap.di

import android.content.Context
import androidx.room.Room
import com.example.zapzap.data.local.AppDatabase
import com.example.zapzap.data.local.dao.ContactDao
import com.example.zapzap.data.local.dao.ConversationDao
import com.example.zapzap.data.local.dao.MessageDao
import com.example.zapzap.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para o banco de dados Room.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "zapzap_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideConversationDao(database: AppDatabase): ConversationDao = database.conversationDao()

    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()
}
