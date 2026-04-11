package ru.akuzyukhin.orientir.server.user.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.user.dto.LoginRequest
import ru.akuzyukhin.orientir.server.user.dto.RefreshRequest
import ru.akuzyukhin.orientir.server.user.dto.RegisterRequest
import ru.akuzyukhin.orientir.server.user.dto.TokenResponse
import ru.akuzyukhin.orientir.server.user.service.AuthService

/**
 * Контроллер аутентификации.
 *
 * Обработка публичных эндпоинтов: регистрация, логин, обновление токенов.
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    /**
     * Регистрация нового пользователя
     *
     * Создание пользователя, профиля роли и возврат пары токенов.
     *
     * @param request данные регистрации
     * @return 201 Created с парой токенов
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<TokenResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Аутентификация пользователя по номеру телефона и паролю.
     *
     * @param request номер телефона и пароль
     * @return 200 OK с парой токенов
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Обновление пары токенов.
     *
     * @param request действующий refresh-токен
     * @return 200 OK с новой парой токенов
     */
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<TokenResponse> {
        val response = authService.refresh(request)
        return ResponseEntity.ok(response)
    }
}