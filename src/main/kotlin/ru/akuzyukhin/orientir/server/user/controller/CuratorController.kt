package ru.akuzyukhin.orientir.server.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.user.dto.AddWardRequest
import ru.akuzyukhin.orientir.server.user.dto.RegisterRequest
import ru.akuzyukhin.orientir.server.user.dto.WardSummary
import ru.akuzyukhin.orientir.server.user.service.CuratorWardService

/**
 * Контроллер куратора.
 *
 * Добавление, просмотр и удаление подопечных.
 */
@RestController
@RequestMapping("/api/v1/curators/me/wards")
class CuratorController(
    private val curatorWardService: CuratorWardService
) {

    @PostMapping
    fun addWard(
        authentication: Authentication,
        @Valid @RequestBody request: AddWardRequest
    ): ResponseEntity<WardSummary> {
        val userId = authentication.principal as Long
        val response = curatorWardService.addWard(userId, request.phoneNumber)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getWards(authentication: Authentication): ResponseEntity<List<WardSummary>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(curatorWardService.getWards(userId))
    }

    @GetMapping("/{wardId}")
    fun getWard(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<WardSummary> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(curatorWardService.getWard(userId, wardId))
    }

    @DeleteMapping("/{wardId}")
    fun removeWard(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        curatorWardService.removeWard(userId, wardId)
        return ResponseEntity.noContent().build()
    }
}