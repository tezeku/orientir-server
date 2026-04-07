package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.NotBlank

/** Тело запроса на обновление пары токенов */
data class RefreshRequest(

    /** Действующий refreh-токен */
    @field:NotBlank(message = "Refresh-токен не может быть пустым")
    val refreshToken: String
)
