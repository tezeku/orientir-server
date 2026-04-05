package ru.akuzyukhin.orientir.server.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.user.entity.Ward

/**
 * Репозиторий для работы с таблицей wards.
 *
 * Является расширением базовой сущности пользователя [ru.akuzyukhin.orientir.server.user.entity.User]
 *  * для пользователей с ролью WARD. Хранит дополнительные данные, такие как адрес проживания.
 */
interface WardRepository : JpaRepository<Ward, Long> {

    /**
     * Поиск подопечного по идентификатору пользователя.
     * Используется для получения профиля подопечного из JWT-токена,
     * в котором хранится userId.
     *
     * @param userId идентификатор записи в таблице users
     * @return найденный подопечный/null
     */
    fun findByUserId(userId: Long): Ward?

    /**
     * Поиск подопечного по номеру телефона связанного пользователя.
     * Используется при добавлении куратором подопечного по номеру телефона.
     *
     * @param phoneNumber номер телефона подопечного
     * @return найденный подопечный/null
     */
    fun findByUserPhoneNumber(phoneNumber: String): Ward?
}