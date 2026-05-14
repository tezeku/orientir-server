package ru.akuzyukhin.orientir.server.task.controller

import jakarta.validation.Valid
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
import ru.akuzyukhin.orientir.server.task.dto.CreateTaskRequest
import ru.akuzyukhin.orientir.server.task.dto.DailyTaskResponse
import ru.akuzyukhin.orientir.server.task.dto.TaskResponse
import ru.akuzyukhin.orientir.server.task.dto.UpdateTaskRequest
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

    /** Создание задачи в расписании */
    @PostMapping("/schedules/{scheduleId}/tasks")
    fun create(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: CreateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        val response = taskService.create(userId, wardId, scheduleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /** Получение списка задач расписания */
    @GetMapping("/schedules/{scheduleId}/tasks")
    fun getAll(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<List<TaskResponse>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.getAllBySchedule(userId, wardId, scheduleId))
    }

    /** Получение конкретной задачи */
    @GetMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun getOne(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.getOne(userId, wardId, scheduleId, taskId))
    }

    /** Обновление задачи */
    @PatchMapping("/schedules/{scheduleId}/tasks/{taskId}")
    fun update(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @PathVariable taskId: Long,
        @Valid @RequestBody request: UpdateTaskRequest
    ): ResponseEntity<TaskResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(taskService.update(userId, wardId, scheduleId, taskId, request))
    }

    /** Удаление задачи */
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

    /** Получение экземпляров задач подопечного на дату */
    @GetMapping("/tasks/daily")
    fun getDailyTasks(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestParam(required = false) date: String?
    ): ResponseEntity<List<DailyTaskResponse>> {
        val userId = authentication.principal as Long
        val targetDate = date?.let { LocalDate.parse(it) } ?: LocalDate.now()
        return ResponseEntity.ok(taskService.getDailyTasksForCurator(userId, wardId, targetDate))
    }
}