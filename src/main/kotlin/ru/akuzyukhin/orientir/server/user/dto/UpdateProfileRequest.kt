package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.Size

/** Тело запроса на частичное обновление профиля */
data class UpdateProfileRequest(

    @field:Size(max = 100, message = "Фамилия не должна превышать 100 символов")
    val surname: String? = null,

    @field:Size(max = 100, message = "Имя не должно превышать 100 символов")
    val name: String? = null,

    @field:Size(max = 100, message = "Отчество не должно превышать 100 символов")
    val patronymic: String? = null,

    val email: String? = null,

    val address: String? = null
)