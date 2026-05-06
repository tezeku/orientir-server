package ru.akuzyukhin.orientir.server.user.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Тело запроса на привязку подопечного к куратору */
data class AddWardRequest(

    @field:NotBlank(message = "Номер телефона подопечного обязателен")
    @field:Size(max = 16, message = "Номер телефона не должен превышать 16 символов")
    val wardPhoneNumber: String
)