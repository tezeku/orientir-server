package ru.akuzyukhin.orientir.server.schedule.controller

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
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.schedule.dto.CreateScheduleRequest
import ru.akuzyukhin.orientir.server.schedule.dto.ScheduleResponse
import ru.akuzyukhin.orientir.server.schedule.dto.UpdateScheduleRequest
import ru.akuzyukhin.orientir.server.schedule.service.ScheduleService

@RestController
@RequestMapping("/api/v1/curators/me/wards/{wardId}/schedules")
class CuratorScheduleController(
    private val scheduleService: ScheduleService
) {

    /** Создание расписания для подопечного */
    @PostMapping
    fun create(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @Valid @RequestBody request: CreateScheduleRequest
    ): ResponseEntity<ScheduleResponse> {
        val userId = authentication.principal as Long
        val response = scheduleService.create(userId, wardId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /** Получение списка расписаний подопечного */
    @GetMapping
    fun getAll(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<List<ScheduleResponse>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getAllByWard(userId, wardId))
    }

    /** Получение конкретного расписания */
    @GetMapping("/{scheduleId}")
    fun getOne(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long
    ): ResponseEntity<ScheduleResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getOne(userId, wardId, scheduleId))
    }

    /** Обновление расписания */
    @PatchMapping("/{scheduleId}")
    fun update(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @PathVariable scheduleId: Long,
        @Valid @RequestBody request: UpdateScheduleRequest
    ): ResponseEntity<ScheduleResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.update(userId, wardId, scheduleId, request))
    }

    /** Удаление расписания */
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