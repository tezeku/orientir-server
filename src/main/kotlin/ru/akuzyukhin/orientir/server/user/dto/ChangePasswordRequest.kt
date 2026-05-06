package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Тело запроса на смену пароля */
data class ChangePasswordRequest(

    @field:NotBlank(message = "Текущий пароль обязателен")
    val currentPassword: String,

    @field:NotBlank(message = "Новый пароль обязателен")
    @field:Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    val newPassword: String
)
