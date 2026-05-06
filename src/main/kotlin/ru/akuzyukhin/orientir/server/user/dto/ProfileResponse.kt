package ru.akuzyukhin.orientir.server.user.dto

/** Ответ с профилем пользователя */
data class ProfileResponse(
    val id: Long,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val role: String,
    val isActive: Boolean,
    val curatorProfile: CuratorProfile? = null,
    val wardProfile: WardProfile? = null
)

data class CuratorProfile(
    val id: Long,
    val email: String
)

data class WardProfile(
    val id: Long,
    val address: String?
)