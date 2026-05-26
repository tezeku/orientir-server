package ru.akuzyukhin.orientir.server.user.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.Role
import ru.akuzyukhin.orientir.server.security.JwtTokenProvider
import ru.akuzyukhin.orientir.server.user.dto.LoginRequest
import ru.akuzyukhin.orientir.server.user.dto.RefreshRequest
import ru.akuzyukhin.orientir.server.user.dto.RegisterRequest
import ru.akuzyukhin.orientir.server.user.dto.TokenResponse
import ru.akuzyukhin.orientir.server.user.entity.Curator
import ru.akuzyukhin.orientir.server.user.entity.User
import ru.akuzyukhin.orientir.server.user.entity.Ward
import ru.akuzyukhin.orientir.server.user.repository.UserRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository

/**
 * Сервис аутентификации и регистрации пользователей.
 * - регистрация с созданием профиля роли;
 * - аутентификация по номеру телефона и паролю;
 * - обновление пары токенов.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${jwt.access-expiration-ms}")
    private val accessExpirationMs: Long
) {

    /**
     * Регистрация нового пользователя.
     * - проверка уникальности номера телефона;
     * - создание записи в таблице users;
     * - создание профиля роли;
     * - генерация пары токенов.
     *
     * @param request данные регистрации
     * @return пара токенов
     * @throws IllegalArgumentException если номер уже зарегистрирован или роль невалидна
     */
    @Transactional
    fun register(request: RegisterRequest): TokenResponse {
        // Проверка уникальности номера телефона
        if (userRepository.existsByPhoneNumber(request.phoneNumber)) {
            throw IllegalArgumentException("Номер телефона: ${request.phoneNumber} уже зарегистрирован")
        }

        // Парсинг роли из строки
        val role = try {
            Role.valueOf(request.role.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Невалидная роль: ${request.role}. Доступные: CURATOR, WARD")
        }

        // Валидация email для куратора
        if (role == Role.CURATOR && request.email.isNullOrBlank()) {
            throw IllegalArgumentException("Email не может быть пустым для роли CURATOR")
        }

        // Создание пользователя с хэшированным паролем
        val user = userRepository.save(
            User(
                surname = request.surname,
                name = request.name,
                patronymic = request.patronymic,
                phoneNumber = request.phoneNumber,
                password = passwordEncoder.encode(request.password)!!,
                role = role
            )
        )

        // Создание профиля роли
        when (role) {
            Role.CURATOR -> curatorRepository.save(
                Curator(user = user, email = request.email!!)
            )
            Role.WARD -> wardRepository.save(
                Ward(user = user, address = request.address)
            )
        }

        // Генерация токенов
        return buildTokenResponse(user)
    }

    /**
     * Аутентификация пользователя.
     * - поиск пользователя по номеру телефона;
     * - проверка пароля;
     * - проверка активности учетной записи;
     * - генерация пары токенов.
     *
     * @param request номер телефона и пароль
     * @return пара токенов
     * @throws IllegalArgumentException если пользователь не найден или пароль неверный
     * @throws IllegalArgumentException если учетная запись деактивирована
     */
    fun login(request: LoginRequest): TokenResponse {
        // Поиск пользователя по номеру телефона
        val user = userRepository.findByPhoneNumber(request.phoneNumber)
            ?: throw IllegalArgumentException("Неверный номер телефона или пароль")

        // Проверка пароля
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Неверный номер телефона или пароль")
        }

        // Проверка активности учетной записи
        if (!user.isActive) {
            throw IllegalArgumentException("Учётная запись деактивирована")
        }

        // Генерация токенов
        return buildTokenResponse(user)
    }

    /**
     * Обновление пары токенов.
     *
     * @param request действующий refresh-токен
     * @return новая пара токенов
     * @throws IllegalArgumentException если refresh-токен невалиден
     */
    fun refresh(request: RefreshRequest): TokenResponse {
        // Валидация refresh-токена
        val claims = try {
            jwtTokenProvider.validateAndGetClaims(request.refreshToken)
        } catch (e: Exception) {
            throw IllegalArgumentException("Невалидный refresh токен")
        }

        // Поиск пользователя
        val userId = jwtTokenProvider.getUserId(claims)
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Пользователь не найден") }

        // Генерация новой пары
        return buildTokenResponse(user)
    }

    /**
     * Формирование ответа с парой токенов.
     *
     * @param user пользователь, для которого генерируются токены
     * @return [TokenResponse] с access-токенов, refresh-токеном и метаданными
     */
    private fun buildTokenResponse(user: User): TokenResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.role.name)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        return TokenResponse(
            userId = user.id,
            role = user.role.name,
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessExpirationMs / 1000
        )
    }
}