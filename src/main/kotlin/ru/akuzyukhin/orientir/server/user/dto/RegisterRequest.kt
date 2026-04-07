package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Тело запроса на регистрацию нового пользователя */
data class RegisterRequest(

    /** Фамилия пользователя */
    @field:NotBlank(message = "Фамилия не может быть пустой")
    @field:Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    val surname: String,

    /** Имя пользователя */
    @field:NotBlank(message = "Имя не может быть пустым")
    @field:Size(max = 100, message = "Имя не должно превышать 100 символов")
    val name: String,

    /** Отчество пользователя */
    @field:Size(max = 100, message = "Отчество не должно превышать 100 символов")
    val patronymic: String? = null,

    /** Номер телефона в формате E.164 */
    @field:NotBlank(message = "Номер телефона не может быть пустым")
    @field:Size(max = 16, message = "Номер телефона не должен превышать 16 символов")
    val phoneNumber: String,

    /** Пароль */
    @field:NotBlank(message = "Пароль не может быть пустым")
    @field:Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    val password: String,

    /** Роль */
    @field:NotBlank(message = "Роль не может отсутствовать")
    val role: String,

    /** Email куратора */
    val email: String? = null,

    /** Адрес подопечного */
    val address: String? = null
)
