package ru.akuzyukhin.orientir.server.monitoring.controller

import jakarta.validation.Valid
import org.antlr.v4.runtime.atn.ATN
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.monitoring.dto.BlockExecutionRequest
import ru.akuzyukhin.orientir.server.monitoring.dto.TaskExecutionResponse
import ru.akuzyukhin.orientir.server.monitoring.service.MonitoringService

/**
 * Контроллер мониторинга выполнения задач.
 *
 * Доступ ограничен ролью подопечного через SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/wards/me/task-executions")
class MonitoringController(
    private val monitoringService: MonitoringService
) {

    /** Отметка выполнения задачи */
    @PostMapping("/{taskExecutionId}/complete")
    fun complete(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long
    ): ResponseEntity<TaskExecutionResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(monitoringService.complete(userId, taskExecutionId))
    }

    /** Осознанные пропуск задачи */
    @PostMapping("/{taskExecutionId}/skip")
    fun skip(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long
    ): ResponseEntity<TaskExecutionResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(monitoringService.skip(userId, taskExecutionId))
    }

    /** Сообщение о невозможности выполнения */
    @PostMapping("/{taskExecutionId}/block")
    fun block(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long,
        @Valid @RequestBody(required = false) request: BlockExecutionRequest?
    ): ResponseEntity<TaskExecutionResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(monitoringService.block(userId, taskExecutionId, request?.comment))
    }
}