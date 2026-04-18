package ru.akuzyukhin.orientir.server.statistics.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.statistics.service.StatisticsService
import java.time.LocalDate

/**
 * Контроллер статистики для подопечного.
 *
 * Доступ ограничен ролью подопечного через SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/wards/me/statistics")
class WardStatisticsController(
    private val statisticsService: StatisticsService
) {

    /**
     * Получение сводки выполнения за период.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param from начало периода
     * @param to конец периода
     * @return 200 OK со сводкой
     */
    @GetMapping("/summary")
    fun getSummary(
        authentication: Authentication,
        @RequestParam from: String,
        @RequestParam to: String
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(
            statisticsService.getSummaryForWard(
                userId, LocalDate.parse(from), LocalDate.parse(to)
            )
        )
    }
}