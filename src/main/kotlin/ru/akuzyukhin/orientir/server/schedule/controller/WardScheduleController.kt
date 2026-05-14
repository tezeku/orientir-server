package ru.akuzyukhin.orientir.server.schedule.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.schedule.dto.ScheduleResponse
import ru.akuzyukhin.orientir.server.schedule.service.ScheduleService

/**
 * Контроллер расписаний для подопечного.
 *
 * Подопечный только просматривает свои расписания.
 */
@RestController
@RequestMapping("/api/v1/wards/me/schedules")
class WardScheduleController(
    private val scheduleService: ScheduleService
) {

    /** Получение списка своих расписаний */
    @GetMapping
    fun getMySchedules(authentication: Authentication): ResponseEntity<List<ScheduleResponse>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getMySchedules(userId))
    }

    /** Просмотр конкретного расписания */
    @GetMapping("/{scheduleId}")
    fun getMySchedule(
        authentication: Authentication,
        @PathVariable scheduleId: Long
    ): ResponseEntity<ScheduleResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(scheduleService.getMySchedule(userId, scheduleId))
    }
}