package ru.akuzyukhin.orientir.server.user.dto

/**
 * Ответ с парой JWT-токенов.
 *
 * @property userId идентификатор пользователя
 * @property role роль пользователя
 * @property accessToken короткоживущий токен для авторизации запросов
 * @property refreshToken долгоживущий токен для обновления пары
 * @property expiresIn время жизни access-токена в секундах
 */
data class TokenResponse(
    val userId: Long,
    val role: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
