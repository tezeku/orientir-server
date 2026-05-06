package ru.akuzyukhin.orientir.server.user.dto

/** Краткие данные куратора для списков */
data class CuratorSummary(
    val id: Long,
    val userId: Long,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val email: String
)
