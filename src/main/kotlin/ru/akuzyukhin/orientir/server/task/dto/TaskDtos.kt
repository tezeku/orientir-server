package ru.akuzyukhin.orientir.server.task.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.TaskType
import java.time.LocalDateTime
import java.time.LocalTime

/** Ответ с данными задачи-шаблона */
data class TaskResponse(
    val id: Long,
    val scheduleId: Long,
    val name: String,
    val type: TaskType,
    val importance: Importance,
    val rrule: String,
    val scheduledTime: LocalTime,
    val windowMinutes: Int
)

/** Запрос на создание задачи */
data class CreateTaskRequest(
    @field:NotBlank(message = "Название задачи не может быть пустым")
    @field:Size(max = 200, message = "Название задачи не может быть длиннее 200 символов")
    val name: String,

    @field:NotNull(message = "Необходимо указать тип задачи")
    val type: TaskType,

    @field:NotNull(message = "Необходимо указать важность")
    val importance: Importance,

    @field:NotBlank(message = "Правило повторения не может быть пустым")
    val rrule: String,

    @field:NotNull(message = "Необходимо указать время выполнения")
    val scheduledTime: LocalTime,

    @field:NotNull(message = "Необходимо указать временное окно")
    @field:Min(value = 1, message = "Временное окно должно быть не менее 1 минуты")
    val windowMinutes: Int
)

/** Запрос на частичное обновление задачи */
data class UpdateTaskRequest(
    @field:Size(max = 200, message = "Название задачи не может быть длиннее 200 символов")
    val name: String? = null,
    val type: TaskType? = null,
    val importance: Importance? = null,
    val rrule: String? = null,
    val scheduledTime: LocalTime? = null,
    @field:Min(value = 1, message = "Временное окно должно быть не менее 1 минуты")
    val windowMinutes: Int? = null
)

/** Вложенная информация о задаче в составе DailyTaskResponse */
data class DailyTaskInfo(
    val id: Long,
    val name: String,
    val type: TaskType,
    val importance: Importance,
    val windowMinutes: Int
)

/** Ответ с данными экземпляра задачи на день */
data class DailyTaskResponse(
    val taskExecutionId: Long,
    val task: DailyTaskInfo,
    val scheduleName: String,
    val scheduledDateTime: LocalDateTime,
    val status: ExecutionStatus,
    val executionTime: LocalDateTime?,
    val deviationMinutes: Int?,
    val isWithinWindow: Boolean?
)