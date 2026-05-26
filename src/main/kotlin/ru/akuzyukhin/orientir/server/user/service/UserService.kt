package ru.akuzyukhin.orientir.server.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.Role
import ru.akuzyukhin.orientir.server.user.dto.CuratorProfile
import ru.akuzyukhin.orientir.server.user.dto.ProfileResponse
import ru.akuzyukhin.orientir.server.user.dto.UpdateProfileRequest
import ru.akuzyukhin.orientir.server.user.dto.WardProfile
import ru.akuzyukhin.orientir.server.user.entity.User
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.UserRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository

/**
 * Сервис управления профилем пользователя.
 * - получение профиля текущего пользователя;
 * - обновление профиля;
 * - смена пароля.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Получение профиля текущего пользователя.
     *
     * @param userId идентификатор пользователя из JWT
     * @return типизированный ProfileResponse
     */
    fun getProfile(userId: Long): ProfileResponse {
        val user = findUserById(userId)

        val curatorProfile = if (user.role == Role.CURATOR) {
            curatorRepository.findByUserId(userId)?.let {
                CuratorProfile(id = it.id, email = it.email)
            }
        } else null

        val wardProfile = if (user.role == Role.WARD) {
            wardRepository.findByUserId(userId)?.let {
                WardProfile(id = it.id, address = it.address)
            }
        } else null

        return ProfileResponse(
            id = user.id,
            surname = user.surname,
            name = user.name,
            patronymic = user.patronymic,
            phoneNumber = user.phoneNumber,
            role = user.role.name,
            isActive = user.isActive,
            curatorProfile = curatorProfile,
            wardProfile = wardProfile
        )
    }

    /**
     * Частичное обновление профиля.
     *
     * @param userId идентификатор пользователя из JWT
     * @param request данные для обновления (любое подмножество полей)
     * @return обновлённый профиль
     */
    @Transactional
    fun updateProfile(userId: Long, request: UpdateProfileRequest): ProfileResponse {
        val user = findUserById(userId)

        request.surname?.let { user.surname = it }
        request.name?.let { user.name = it }
        request.patronymic?.let { user.patronymic = it }

        userRepository.save(user)

        when (user.role) {
            Role.CURATOR -> {
                request.email?.let { email ->
                    val curator = curatorRepository.findByUserId(userId)
                        ?: throw IllegalStateException("Профиль куратора не найден")
                    curator.email = email
                    curatorRepository.save(curator)
                }
            }
            Role.WARD -> {
                request.address?.let { address ->
                    val ward = wardRepository.findByUserId(userId)
                        ?: throw IllegalStateException("Профиль подопечного не найден")
                    ward.address = address
                    wardRepository.save(ward)
                }
            }
        }

        return getProfile(userId)
    }

    /**
     * Смена пароля.
     *
     * @param userId идентификатор пользователя из JWT
     * @param currentPassword текущий пароль для проверки
     * @param newPassword новый пароль
     * @throws IllegalArgumentException если неверен текущий или невалиден новый пароль
     */
    @Transactional
    fun changePassword(userId: Long, currentPassword: String, newPassword: String) {
        val user = findUserById(userId)

        // Проверка текущего пароля
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("Неверный текущий пароль")
        }

        // Валидация нового пароля
        if (newPassword.length < 8) {
            throw IllegalArgumentException("Пароль должен содержать минимум 8 символов")
        }

        // Сохранение нового хэша
        user.password = passwordEncoder.encode(newPassword)!!
        userRepository.save(user)
    }

    /**
     * Поиск пользователя по id
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    private fun findUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Пользователь не найден") }
    }
}