package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.NotBlank

/** Тело запроса на вход в систему */
data class LoginRequest(

    /** Номер телефона - логин */
    @field:NotBlank(message = "Номер телефона не может быть пустым")
    val phoneNumber: String,

    /** Пароль */
    @field:NotBlank(message = "Пароль не может быть пустым")
    val password: String,
)
