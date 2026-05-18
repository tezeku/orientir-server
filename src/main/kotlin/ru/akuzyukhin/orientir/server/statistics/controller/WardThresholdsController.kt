package ru.akuzyukhin.orientir.server.statistics.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.statistics.dto.UpdateWardThresholdsRequest
import ru.akuzyukhin.orientir.server.statistics.dto.WardThresholdsResponse
import ru.akuzyukhin.orientir.server.statistics.service.WardThresholdsService

/** REST-эндпоинты управления порогами нарушений подопечного */
@RestController
@RequestMapping("/api/v1/curators/me/wards/{wardId}/thresholds")
class CuratorWardThresholdsController(
    private val thresholdsService: WardThresholdsService
) {

    /** Получение текущих порогов нарушений (создаются автоматически при первом запросе) */
    @GetMapping
    fun get(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<WardThresholdsResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(thresholdsService.getOrCreate(userId, wardId))
    }

    /** Частичное обновление порогов */
    @PatchMapping
    fun update(
        authentication: Authentication,
        @PathVariable wardId: Long,
        @Valid @RequestBody request: UpdateWardThresholdsRequest
    ): ResponseEntity<WardThresholdsResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(thresholdsService.update(userId, wardId, request))
    }
}