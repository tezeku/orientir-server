package ru.akuzyukhin.orientir.server.schedule.controller

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
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.schedule.service.ScheduleService

@RestController
@RequestMapping("/api/v1/curators/me/wards/{wardId}/schedules")
class CuratorScheduleController(
    private val scheduleService: ScheduleService
) {

    /**
     * Создание расписания для подопечного.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param request map с полем name
     * @return 201 Created с данными расписания
     */
    @PostMapping
    fun create(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val name = request["name"]
            ?: throw IllegalArgumentException("Название расписания не может быть пустым")

        val response = scheduleService.create(userId, wardId, name)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Получение списка расписаний подопечного.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @return 200 OK со списком расписаний
     */
    @GetMapping
    fun getAll(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<List<Map<String, Any?>>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getAllByWard(userId, wardId))
    }

    /**
     * Получение конкретного расписания.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @return 200 OK с данными расписания
     */
    @GetMapping("/{scheduleId}")
    fun getOne(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getOne(userId, wardId, scheduleId))
    }

    /**
     * Обновление расписания.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param request map с полем name
     * @return 200 OK с обновленными данными
     */
    @PatchMapping("/{scheduleId}")
    fun update(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val name = request["name"]
            ?: throw IllegalArgumentException("Название расписания не может быть пустым")

        return ResponseEntity.ok(scheduleService.update(userId, wardId, scheduleId, name))
    }

    /**
     * Удаление расписания.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @return 204 No Content
     */
    @DeleteMapping("/{scheduleId}")
    fun delete(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        scheduleService.delete(userId, wardId, scheduleId)
        return ResponseEntity.noContent().build()
    }
}