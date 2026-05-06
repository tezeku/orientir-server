package ru.akuzyukhin.orientir.server.user.dto

/** Краткие данные подопечного для списков и точечных запросов */
data class WardSummary(
    val id: Long,
    val userId: Long,
    val surname: String,
    val name: String,
    val patronymic: String?,
    val phoneNumber: String,
    val address: String?
)