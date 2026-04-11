package ru.akuzyukhin.orientir.server.user.controller

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
import ru.akuzyukhin.orientir.server.user.dto.RegisterRequest
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

    /**
     * Привязка подопечного к куратору по номеру телефона.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param request map с полем wardPhoneNumber
     * @return 201 Created с данными связи
     */
    @PostMapping
    fun addWard(
        authentication: Authentication,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val phoneNumber = request["wardPhoneNumber"]
            ?: throw IllegalArgumentException("Номер телефона подопечного обязателен")

        val response = curatorWardService.addWard(userId, phoneNumber)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Получение списка всех подопечных текущего куратора.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @return 200 OK со списком подопечных
     */
    @GetMapping
    fun getWards(authentication: Authentication): ResponseEntity<List<Map<String, Any?>>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(curatorWardService.getWards(userId))
    }

    /**
     * Получение информации о конкретном подопечном.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @return 200 OK с данными подопечного
     */
    @GetMapping("/{wardId}")
    fun getWard(
        authentication: Authentication,
        @PathVariable wardId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(curatorWardService.getWard(userId, wardId))
    }

    /**
     * Удаление связи с подопечным.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param wardId идентификатор подопечного
     * @return 204 No Content
     */
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