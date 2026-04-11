package ru.akuzyukhin.orientir.server.user.service

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.Role
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
     * @return map с данными профиля
     */
    fun getProfile(userId: Long): Map<String, Any?> {
        val user = findUserById(userId)

        val profile = mutableMapOf<String, Any?>(
            "id" to user.id,
            "surname" to user.surname,
            "name" to user.name,
            "patronymic" to user.patronymic,
            "phoneNumber" to user.phoneNumber,
            "role" to user.role.name,
            "isActive" to user.isActive
        )

        // Добавление данных роли
        when (user.role) {
            Role.CURATOR -> {
                val curator = curatorRepository.findByUserId(userId)
                profile["curatorProfile"] = curator?.let {
                    mapOf("id" to it.id, "email" to it.email)
                }
            }
            Role.WARD -> {
                val ward = wardRepository.findByUserId(userId)
                profile["wardProfile"] = ward?.let {
                    mapOf("id" to it.id, "address" to it.address)
                }
            }
        }
        return profile
    }

    /**
     * Обновление профиля.
     *
     * @param userId идентификатор пользователя из JWT
     * @param updates map с  обновляемыми полями
     * @return обновленный профиль
     */
    @Transactional
    fun updateProfile(userId: Long, updates: Map<String, String?>): Map<String, Any?> {
        val user = findUserById(userId)

        // Обновление общих полей
        updates["surname"]?.let { user.surname = it }
        updates["name"]?.let { user.name = it }
        if (updates.containsKey("patronymic")) {
            user.patronymic = updates["patronymic"]
        }

        userRepository.save(user)

        // Обновление полей роли
        when (user.role) {
            Role.CURATOR -> {
                updates["email"]?.let { email ->
                    val curator = curatorRepository.findByUserId(userId)
                        ?: throw IllegalStateException("Профиль куратора не найден")
                    curator.email = email
                    curatorRepository.save(curator)
                }
            }
            Role.WARD -> {
                if (updates.containsKey("address")) {
                    val ward = wardRepository.findByUserId(userId)
                        ?: throw IllegalStateException("Профиль подопечного не найден")
                    ward.address = updates["address"]
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
        if (newPassword .length < 8) {
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
     * @return найденный пользовтаель
     * @throws IllegalArgumentException если пользователь не найден
     */
    private fun findUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Пользователь не найден") }
    }
}