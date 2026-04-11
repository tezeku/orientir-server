package ru.akuzyukhin.orientir.server.user.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.user.service.UserService

/**
 * Контроллер управления профилем пользователя.
 *
 * Обработка запросов текущего аутентифицированного пользователя.
 */
@RestController
@RequestMapping("/api/v1/users/me")
class UserController(
    private val userService: UserService
) {

    /**
     * Получение профиля текущего пользователя.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @return 200 OK с данными профиля
     */
    @GetMapping
    fun getProfile(authentication: Authentication): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(userService.getProfile(userId))
    }

    /**
     * Обновление профиля.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param updates
     */
    @PatchMapping
    fun updateProfile(
        authentication: Authentication,
        @RequestBody updates: Map<String, String?>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(userService.updateProfile(userId, updates))
    }

    /**
     * Смена пароля.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param request map с полями currentPassword и newPassword
     * @return 204 No Content при успешной смене
     */
    @PostMapping("/password")
    fun changePassword(
        authentication: Authentication,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        val currentPassword = request["currentPassword"]
            ?: throw IllegalArgumentException("Текущий пароль обязателен")
        val newPassword = request["newPassword"]
            ?: throw IllegalArgumentException("Новый пароль обязателен")

        userService.changePassword(userId, currentPassword, newPassword)
        return ResponseEntity.noContent().build()
    }
}