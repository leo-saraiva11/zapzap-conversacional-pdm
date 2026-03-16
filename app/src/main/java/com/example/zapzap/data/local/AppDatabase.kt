package com.example.zapzap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.zapzap.data.local.dao.ContactDao
import com.example.zapzap.data.local.dao.ConversationDao
import com.example.zapzap.data.local.dao.MessageDao
import com.example.zapzap.data.local.dao.UserDao
import com.example.zapzap.data.local.entity.ContactEntity
import com.example.zapzap.data.local.entity.ConversationEntity
import com.example.zapzap.data.local.entity.MessageEntity
import com.example.zapzap.data.local.entity.UserEntity

/**
 * Base de dados Room do ZapZap.
 * Armazena dados localmente para suporte offline e cache.
 */
@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ConversationEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun contactDao(): ContactDao
}
