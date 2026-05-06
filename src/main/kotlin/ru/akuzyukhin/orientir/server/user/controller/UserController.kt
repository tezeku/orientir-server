package ru.akuzyukhin.orientir.server.user.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.user.dto.ChangePasswordRequest
import ru.akuzyukhin.orientir.server.user.dto.ProfileResponse
import ru.akuzyukhin.orientir.server.user.dto.UpdateProfileRequest
import ru.akuzyukhin.orientir.server.user.service.UserService

/** Контроллер управления профилем пользователя */
@RestController
@RequestMapping("/api/v1/users/me")
class UserController(
    private val userService: UserService
) {

    /**
     * Получение профиля текущего пользователя.
     *
     * @return 200 OK с типизированным ProfileResponse
     */
    @GetMapping
    fun getProfile(authentication: Authentication): ResponseEntity<ProfileResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(userService.getProfile(userId))
    }

    /**
     * Частичное обновление профиля.
     *
     * @return 200 OK с обновлённым профилем
     */
    @PatchMapping
    fun updateProfile(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ProfileResponse> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(userService.updateProfile(userId, request))
    }

    /**
     * Смена пароля.
     *
     * @return 204 No Content при успешной смене
     */
    @PostMapping("/password")
    fun changePassword(
        authentication: Authentication,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {
        val userId = authentication.principal as Long
        userService.changePassword(userId, request.currentPassword, request.newPassword)
        return ResponseEntity.noContent().build()
    }
}