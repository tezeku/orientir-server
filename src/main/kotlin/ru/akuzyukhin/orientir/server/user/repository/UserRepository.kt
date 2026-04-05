package ru.akuzyukhin.orientir.server.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.user.entity.User

/**
 * Репозиторий для работы с таблицей users.
 *
 * Предоставляет стандартные CRUD-операции через [JpaRepository],
 * а также методы посика пользователя по номеру телефона для
 * аутентификации и проверки уникальности при регистрации.
 */
interface UserRepository : JpaRepository<User, Long> {

    /**
     * Поиск пользователя по номеру телефона.
     * Используется при аутентификации.
     *
     * @param phoneNumber номер телефона в формате E.164, например "+79223334455"
     * @return найденный пользователь/null
     */
    fun findByUsername(phoneNumber: String): User?

    /**
     * Проверка существования пользователя с указанным номером телефона.
     * Используется при регистрации.
     *
     * @param phoneNumber номер телефона для проверки
     * @return true - пользователь с таким номером уже существует
     */
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}