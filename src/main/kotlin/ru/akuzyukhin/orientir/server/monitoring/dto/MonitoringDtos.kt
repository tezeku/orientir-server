package ru.akuzyukhin.orientir.server.monitoring.dto

import jakarta.validation.constraints.Size
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import java.time.LocalDateTime

/** Ответ с данными экземпляра задачи после действия мониторинга */
data class TaskExecutionResponse(
    val id: Long,
    val taskId: Long,
    val scheduledDateTime: LocalDateTime,
    val executionTime: LocalDateTime?,
    val status: ExecutionStatus,
    val deviationMinutes: Int?,
    val isWithinWindow: Boolean?
)

/** Запрос на блокировку задачи с опциональным комментарием */
data class BlockExecutionRequest(
    @field:Size(max = 500, message = "Комментарий не может быть длиннее 500 символов")
    val comment: String? = null
)