package ru.akuzyukhin.orientir.server.task.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.task.service.TaskService
import java.time.LocalDate

/**
 * Контроллер задач для подопечного.
 *
 * Только чтение - подопечный просматривает свои задачи
 * на день и информацию о конкретной задаче.
 */
@RestController
@RequestMapping("/api/v1/wards/me")
class WardTaskController(
    private val taskService: TaskService
) {

    /**
     * Получение задач на день для подопечного.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param date дата
     * @return 200 OK со списком экземпляров задач
     */
    @GetMapping("/tasks/daily")
    fun getDailyTasks(
        authentication: Authentication,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<List<Map<String, Any?>>> {
        val userId = authentication.principal as Long
        val targetDate = date?.let { LocalDate.parse(it) } ?: LocalDate.now()
        return ResponseEntity.ok(taskService.getDailyTasksForWard(userId, targetDate))
    }

    /**
     * Получение конкретной задачи-шаблона.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param scheduleId идентификатор расписания
     * @param taskId идентификатор задачи
     * @return 200 OK с данными задачи
     */
    @GetMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun getTask(
        authentication: Authentication,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.getTaskForWard(userId, scheduleId, taskId))
    }
}