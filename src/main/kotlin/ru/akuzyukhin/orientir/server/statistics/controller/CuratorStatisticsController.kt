package ru.akuzyukhin.orientir.server.statistics.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.statistics.service.StatisticsService
import java.time.LocalDate

/**
 * Контроллер статистики для куратора.
 *
 * Доступ ограничен ролью куратора через SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/curators/me/wards/{wardId}/statistics")
class CuratorStatisticsController(
    private val statisticsService: StatisticsService
) {

    /**
     * Получение сводки выполнения за период.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return 200 OK с агрегированной сводкой
     */
    @GetMapping("/summary")
    fun getSummary(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestParam from: String,
        @RequestParam to: String
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(
            statisticsService.getSummaryForCurator(
                userId, wardId, LocalDate.parse(from), LocalDate.parse(to)
            )
        )
    }

    /**
     * Получение статистики отклонений по важности и типу задач.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return 200 OK с детальной статистикой
     */
    @GetMapping("/deviations")
    fun getDeviations(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestParam from: String,
        @RequestParam to: String
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(
            statisticsService.getDeviations(
                userId, wardId, LocalDate.parse(from), LocalDate.parse(to)
            )
        )
    }

    /**
     * Получение глобального отклонения.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return 200 OK с глобальным отклонением и порогом
     */
    @GetMapping("/global-deviation")
    fun getGlobalDeviation(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @RequestParam from: String,
        @RequestParam to: String
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(
            statisticsService.getGlobalDeviation(
                userId, wardId, LocalDate.parse(from), LocalDate.parse(to)
            )
        )
    }
}