package ru.akuzyukhin.orientir.server.schedule.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/** Ответ с данными расписания */
data class ScheduleResponse(
    val id: Long,
    val name: String,
    val wardId: Long
)

/** Запрос на создание расписания */
data class CreateScheduleRequest(
    @field:NotBlank(message = "Название расписания не может быть пустым")
    @field:Size(max = 100, message = "Название расписания не может быть длиннее 100 символов")
    val name: String
)

/** Запрос на обновление расписания */
data class UpdateScheduleRequest(
    @field:NotBlank(message = "Название расписания не может быть пустым")
    @field:Size(max = 100, message = "Название расписания не может быть длиннее 100 символов")
    val name: String
)
