package com.example.zapzap.data.mapper

import com.example.zapzap.data.local.entity.UserEntity
import com.example.zapzap.domain.model.User
import com.example.zapzap.domain.model.UserStatus

/**
 * Mapper para converter entre UserEntity (Room) e User (Domain).
 */
object UserMapper {
    fun toDomain(entity: UserEntity): User = User(
        uid = entity.uid,
        displayName = entity.displayName,
        email = entity.email,
        phone = entity.phone,
        photoUrl = entity.photoUrl,
        status = UserStatus.valueOf(entity.status),
        lastSeen = entity.lastSeen,
        fcmToken = entity.fcmToken,
        publicKey = entity.publicKey,
        about = entity.about
    )

    fun toEntity(user: User): UserEntity = UserEntity(
        uid = user.uid,
        displayName = user.displayName,
        email = user.email,
        phone = user.phone,
        photoUrl = user.photoUrl,
        status = user.status.name,
        lastSeen = user.lastSeen,
        fcmToken = user.fcmToken,
        publicKey = user.publicKey,
        about = user.about
    )

    fun fromFirestore(map: Map<String, Any?>, uid: String): User = User(
        uid = uid,
        displayName = map["displayName"] as? String ?: "",
        email = map["email"] as? String ?: "",
        phone = map["phone"] as? String ?: "",
        photoUrl = map["photoUrl"] as? String ?: "",
        status = try { UserStatus.valueOf(map["status"] as? String ?: "OFFLINE") } catch (e: Exception) { UserStatus.OFFLINE },
        lastSeen = map["lastSeen"] as? Long ?: 0L,
        fcmToken = map["fcmToken"] as? String ?: "",
        publicKey = map["publicKey"] as? String ?: "",
        about = map["about"] as? String ?: "Olá! Estou usando o ZapZap."
    )

    fun toFirestore(user: User): Map<String, Any?> = mapOf(
        "displayName" to user.displayName,
        "email" to user.email,
        "phone" to user.phone,
        "photoUrl" to user.photoUrl,
        "status" to user.status.name,
        "lastSeen" to user.lastSeen,
        "fcmToken" to user.fcmToken,
        "publicKey" to user.publicKey,
        "about" to user.about
    )
}
