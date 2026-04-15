package ru.akuzyukhin.orientir.server.task.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.task.service.TaskService
import java.time.LocalDate

/**
 * Контроллер задач для куратора.
 *
 * CRUD-операции над задачами в расписании подопечного
 * и просмотр экземпляров задач на дату. Доступ ограничен
 * ролью куратора через SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/curators/me/wards/{wardId}")
class CuratorTaskController(
    private val taskService: TaskService
) {

    /**
     * Создание задачи в расписании.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param request параметры задачи
     * @return 201 Created с данными задачи
     */
    @PostMapping("/schedules/{scheduleId}/tasks")
    fun create(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val response = taskService.create(userId, wardId, scheduleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Получение списка задач расписания.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @return 200 OK со списком задач
     */
    @GetMapping("/schedules/{scheduleId}/tasks")
    fun getAll(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<List<Map<String, Any?>>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.getAllBySchedule(userId, wardId, scheduleId))
    }

    /**
     * Получение конкретной задачи.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param taskId идентификатор задачи
     * @return 200 OK с данными задачи
     */
    @GetMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun getOne(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.getOne(userId, wardId, scheduleId, taskId))
    }

    /**
     * Обновление задачи.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param taskId идентификатор задачи
     * @param updates map с обновляемыми полями
     * @return 200 OK с обновленными данными
     */
    @PatchMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun update(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long,
        @RequestBody updates: Map<String, String>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.update(userId, wardId, scheduleId, taskId, updates))
    }

    /**
     * Удаление задачи.
     *
     * @param authentucation объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param taskId идентификатор задачи
     * @return 204 No Content
     */
    @DeleteMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun delete(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        taskService.delete(userId, wardId, scheduleId, taskId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Получение экземпляров задач подопечного на дату.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификтаор подопечного
     * @param date дата
     * @return 200 OK со списком экземпляров задач
     */
    @GetMapping("/tasks/daily")
    fun getDailyTasks(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<List<Map<String, Any?>>> {
        val userId = authentication.principal as Long
        val targetDate = date?.let { LocalDate.parse(it) } ?: LocalDate.now()
        return ResponseEntity.ok(taskService.getDailyTasksForCurator(userId, wardId, targetDate))
    }
}