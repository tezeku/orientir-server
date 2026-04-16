package ru.akuzyukhin.orientir.server.monitoring.controller

import org.antlr.v4.runtime.atn.ATN
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    /**
     * Отметка выполнения задачи.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param taskExecutionId идентификатор экземпляра задачи
     * @return 200 OK с обновленными данными
     */
    @PostMapping("/{taskExecutionId}/complete")
    fun complete(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(monitoringService.complete(userId, taskExecutionId))
    }

    /**
     * Осознанные пропуск задачи.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param taskExecutionId идентификатор экземпляра задачи
     * @return 200 OK с обновленными данными
     */
    @PostMapping("/{taskExecutionId}/skip")
    fun skip(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(monitoringService.skip(userId, taskExecutionId))
    }

    /**
     * Сообщение о невозможности выполнения.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param taskExecutionId идентификатор экземпляра задачи
     * @param request map с опциональным полем
     * @return 200 OK с обновленными данными
     */
    @PostMapping("/{taskExecutionId}/block")
    fun block(
        authentication: Authentication,
        @PathVariable taskExecutionId: Long,
        @RequestBody(required = false) request: Map<String, String>?
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val comment = request?.get("comment")
        return ResponseEntity.ok(monitoringService.block(userId, taskExecutionId, comment))
    }
}